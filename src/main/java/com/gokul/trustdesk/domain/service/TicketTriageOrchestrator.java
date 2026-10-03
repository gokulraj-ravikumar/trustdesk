package com.gokul.trustdesk.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gokul.trustdesk.application.rest.dto.DocumentSearchResponse;
import com.gokul.trustdesk.application.rest.dto.TicketContextResponse;
import com.gokul.trustdesk.application.rest.exception.ResourceNotFoundException;
import com.gokul.trustdesk.domain.model.DraftDecision;
import com.gokul.trustdesk.domain.model.TriageDecision;
import com.gokul.trustdesk.domain.model.enums.*;
import com.gokul.trustdesk.domain.port.AiLanguageModelPort;
import com.gokul.trustdesk.infrastructure.persistence.entity.AgentRunTrace;
import com.gokul.trustdesk.infrastructure.persistence.entity.DraftReply;
import com.gokul.trustdesk.infrastructure.persistence.entity.Ticket;
import com.gokul.trustdesk.infrastructure.persistence.entity.ToolActionRequest;
import com.gokul.trustdesk.infrastructure.persistence.repository.AgentRunTraceRepository;
import com.gokul.trustdesk.infrastructure.persistence.repository.DraftReplyRepository;
import com.gokul.trustdesk.infrastructure.persistence.repository.TicketRepository;
import com.gokul.trustdesk.infrastructure.persistence.repository.ToolActionRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class TicketTriageOrchestrator {

    private final TicketContextService ticketContextService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final AiLanguageModelPort aiLanguageModelPort; // Interfaces with Gemini/Mock
    private final TicketRepository ticketRepository;
    private final DraftReplyRepository draftReplyRepository;
    private final AdversarialGuardrailService guardrailService;
    private final ToolActionRequestRepository toolActionRequestRepository;
    private final ObjectMapper objectMapper;
    private final AgentRunTraceRepository traceRepository;

    public TicketTriageOrchestrator(TicketContextService ticketContextService,
                                    KnowledgeBaseService knowledgeBaseService,
                                    AiLanguageModelPort aiLanguageModelPort,
                                    TicketRepository ticketRepository,
                                    DraftReplyRepository draftReplyRepository,
                                    AdversarialGuardrailService guardrailService,
                                    ToolActionRequestRepository toolActionRequestRepository,
                                    ObjectMapper objectMapper,
                                    AgentRunTraceRepository traceRepository) {
        this.ticketContextService = ticketContextService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.aiLanguageModelPort = aiLanguageModelPort;
        this.ticketRepository = ticketRepository;
        this.draftReplyRepository = draftReplyRepository;
        this.guardrailService = guardrailService;
        this.toolActionRequestRepository = toolActionRequestRepository;
        this.objectMapper = objectMapper;
        this.traceRepository = traceRepository;
    }

    @Transactional
    public TriageDecision triageTicket(String ticketId) {
        // 1. Fetch full context
        TicketContextResponse context = ticketContextService.getTicketContext(ticketId);

        // 2. Call the AI Adapter to categorize the ticket
        TriageDecision decision = aiLanguageModelPort.triageTicket(context);

        // 3. Save the results back to the database
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        ticket.setCategory(TicketCategory.valueOf(decision.category().toUpperCase()));
        ticket.setPriority(TicketPriority.valueOf(decision.priority().toUpperCase()));
        ticket.setTriageReason(decision.reasonSummary());

        // If the decision indicates escalation, update the ticket status
        if (Boolean.TRUE.equals(decision.shouldEscalate())) {
            ticket.setStatus(TicketStatus.ESCALATED);
        } else {
            ticket.setStatus(TicketStatus.TRIAGED);
        }

        ticketRepository.save(ticket);

        RunStatus runStatus = Boolean.TRUE.equals(decision.shouldEscalate()) ? RunStatus.ESCALATED : RunStatus.SUCCESS;

        saveTrace(ticket, RunType.TRIAGE, runStatus, GuardrailResult.PASSED, List.of(), List.of());

        return decision;
    }

    @Transactional
    public DraftDecision generateDraftReply(String ticketId) {
        TicketContextResponse context = ticketContextService.getTicketContext(ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        // Guardrail check: If the ticket body contains adversarial content, escalate and return a safe response
        if (!guardrailService.isSafe(context.ticket().body())) {
            ticket.setStatus(TicketStatus.ESCALATED);
            ticket.setTriageReason("BLOCKED BY GUARDRAIL: Adversarial intent detected in ticket body.");
            ticketRepository.save(ticket);

            String cannedResponse = "For security reasons, your request requires manual review. It has been escalated to a human agent.";
            saveDraftToDatabase(ticket, cannedResponse, List.of());

            // Log adversarial intercept as BLOCKED and ESCALATED
            saveTrace(ticket, RunType.DRAFT_REPLY, RunStatus.ESCALATED, GuardrailResult.BLOCKED, List.of(), List.of());

            return new DraftDecision(cannedResponse, List.of(), List.of());
        }

        // 1. Ask Postgres to find the most relevant policy documents based on the customer's message
        List<DocumentSearchResponse> retrievedDocs = knowledgeBaseService.search(context.ticket().body());

        // 2. Call the AI Adapter to write the response using the context and the docs
        DraftDecision decision = aiLanguageModelPort.generateDraft(context, retrievedDocs);

        // 3. Save the draft reply to the DB
        saveDraftToDatabase(ticket, decision.draftBody(), decision.citations());

        // --- APPROVAL-GATED TOOL GENERATION ---
        if (decision.recommendedActions() != null && !decision.recommendedActions().isEmpty()) {
            for (String actionName : decision.recommendedActions()) {
                Optional<ToolRegistry> toolOpt = ToolRegistry.fromName(actionName);

                if (toolOpt.isPresent()) {
                    createPendingToolAction(ticket, toolOpt.get());
                    break;
                }
            }
        }

        // Agent Run Trace Logging
        RunStatus draftRunStatus = TicketStatus.ESCALATED.equals(ticket.getStatus())
                ? RunStatus.ESCALATED
                : RunStatus.SUCCESS;

        List<String> docIds = retrievedDocs.stream()
                .map(DocumentSearchResponse::docId)
                .toList();

        saveTrace(
                ticket,
                RunType.DRAFT_REPLY,
                draftRunStatus,
                GuardrailResult.PASSED,
                docIds,
                decision.recommendedActions() != null ? decision.recommendedActions() : List.of()
        );

        return decision;
    }

    private void saveDraftToDatabase(Ticket ticket, String body, List<String> citations) {
        DraftReply draftEntity = new DraftReply();

        draftEntity.setId("drf_" + UUID.randomUUID().toString().substring(0, 8));
        draftEntity.setTicket(ticket);
        draftEntity.setBody(body);
        draftEntity.setStatus(DraftStatus.GENERATED);
        draftEntity.setCreatedAt(Instant.now());
        draftEntity.setCitations(citations);

        draftReplyRepository.save(draftEntity);
    }

    // Helper method to create the pending tool action
    private void createPendingToolAction(Ticket ticket, ToolRegistry toolDef) {
        // --- IDEMPOTENCY GUARD ---
        // Check if there is already a pending action for this exact tool on this ticket
        boolean alreadyPending = toolActionRequestRepository.existsByTicketIdAndToolNameAndStatusIn(
                ticket.getId(),
                toolDef.getToolName(),
                List.of(ActionStatus.APPROVAL_REQUIRED, ActionStatus.REQUESTED)
        );

        if (alreadyPending) {
            return;
        }

        ToolActionRequest action = new ToolActionRequest();

        String idempotencyKey = UUID.randomUUID().toString();

        action.setId("act_" + idempotencyKey.substring(0, 8));
        action.setTicket(ticket);

        action.setToolName(toolDef.getToolName());
        action.setIdempotencyKey(idempotencyKey);
        action.setRequiresHumanApproval(toolDef.isRequiresApproval());
        action.setRiskLevel(toolDef.getRiskLevel());
        action.setStatus(toolDef.getInitialStatus());
        action.setCreatedAt(Instant.now());

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("customerId", ticket.getCustomer().getId());
        if (ticket.getOrder() != null) {
            payloadMap.put("orderId", ticket.getOrder().getId());
        }
        action.setPayload(payloadMap);

        toolActionRequestRepository.save(action);
    }

    // --- HELPER METHOD TO SAVE TRACES ---
    private void saveTrace(Ticket ticket,
                           RunType runType,
                           RunStatus status,
                           GuardrailResult guardrailResult,
                           List<String> retrievedDocIds,
                           List<String> toolCalls) {
        AgentRunTrace trace = new AgentRunTrace();
        trace.setId("run_" + UUID.randomUUID().toString().substring(0, 8));
        trace.setTicket(ticket);
        trace.setRunType(runType);
        trace.setStatus(status);
        trace.setGuardrailResults(guardrailResult);
        trace.setRetrievedDocIds(retrievedDocIds != null ? retrievedDocIds : List.of());
        trace.setToolCalls(toolCalls != null ? toolCalls : List.of());
        trace.setCreatedAt(Instant.now());

        traceRepository.save(trace);
    }
}
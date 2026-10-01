package com.gokul.trustdesk.domain.service;

import com.gokul.trustdesk.application.rest.dto.DocumentSearchResponse;
import com.gokul.trustdesk.application.rest.dto.TicketContextResponse;
import com.gokul.trustdesk.application.rest.exception.ResourceNotFoundException;
import com.gokul.trustdesk.domain.model.DraftDecision;
import com.gokul.trustdesk.domain.model.TriageDecision;
import com.gokul.trustdesk.domain.model.enums.DraftStatus;
import com.gokul.trustdesk.domain.model.enums.TicketCategory;
import com.gokul.trustdesk.domain.model.enums.TicketPriority;
import com.gokul.trustdesk.domain.model.enums.TicketStatus;
import com.gokul.trustdesk.domain.port.AiLanguageModelPort;
import com.gokul.trustdesk.infrastructure.persistence.entity.DraftReply;
import com.gokul.trustdesk.infrastructure.persistence.entity.Ticket;
import com.gokul.trustdesk.infrastructure.persistence.repository.DraftReplyRepository;
import com.gokul.trustdesk.infrastructure.persistence.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TicketTriageOrchestrator {

    private final TicketContextService ticketContextService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final AiLanguageModelPort aiLanguageModelPort; // Interfaces with Gemini/Mock
    private final TicketRepository ticketRepository;
    private final DraftReplyRepository draftReplyRepository;
    private final AdversarialGuardrailService guardrailService;

    public TicketTriageOrchestrator(TicketContextService ticketContextService,
                                    KnowledgeBaseService knowledgeBaseService,
                                    AiLanguageModelPort aiLanguageModelPort,
                                    TicketRepository ticketRepository,
                                    DraftReplyRepository draftReplyRepository,
                                    AdversarialGuardrailService guardrailService) {
        this.ticketContextService = ticketContextService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.aiLanguageModelPort = aiLanguageModelPort;
        this.ticketRepository = ticketRepository;
        this.draftReplyRepository = draftReplyRepository;
        this.guardrailService = guardrailService;
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

            return new DraftDecision(cannedResponse, List.of(), List.of());
        }

        // 1. Ask Postgres to find the most relevant policy documents based on the customer's message
        List<DocumentSearchResponse> retrievedDocs = knowledgeBaseService.search(context.ticket().body());

        // 2. Call the AI Adapter to write the response using the context and the docs
        DraftDecision decision = aiLanguageModelPort.generateDraft(context, retrievedDocs);

        // 3. Save the draft reply to the DB
        saveDraftToDatabase(ticket, decision.draftBody(), decision.citations());

        // need to add the logic here to save this draft and handle the approval gate.
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
}
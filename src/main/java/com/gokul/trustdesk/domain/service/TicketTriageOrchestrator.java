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

import java.util.List;
import java.util.UUID;

@Service
public class TicketTriageOrchestrator {

    private final TicketContextService ticketContextService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final AiLanguageModelPort aiLanguageModelPort; // Interfaces with Gemini/Mock
    private final TicketRepository ticketRepository;
    private final DraftReplyRepository draftReplyRepository;

    public TicketTriageOrchestrator(TicketContextService ticketContextService,
                                    KnowledgeBaseService knowledgeBaseService,
                                    AiLanguageModelPort aiLanguageModelPort,
                                    TicketRepository ticketRepository,
                                    DraftReplyRepository draftReplyRepository) {
        this.ticketContextService = ticketContextService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.aiLanguageModelPort = aiLanguageModelPort;
        this.ticketRepository = ticketRepository;
        this.draftReplyRepository = draftReplyRepository;
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

        // If the decision indicates escalation, update the ticket status and reason
        if (Boolean.TRUE.equals(decision.shouldEscalate())) {
            ticket.setStatus(TicketStatus.ESCALATED);
            ticket.setEscalationReason(decision.reasonSummary());
        } else {
            ticket.setStatus(TicketStatus.TRIAGED);
        }

        ticketRepository.save(ticket);

        return decision;
    }

    @Transactional
    public DraftDecision generateDraftReply(String ticketId) {
        TicketContextResponse context = ticketContextService.getTicketContext(ticketId);

        // 1. Ask Postgres to find the most relevant policy documents based on the customer's message
        List<DocumentSearchResponse> retrievedDocs = knowledgeBaseService.search(context.ticket().body());

        // 2. Call the AI Adapter to write the response using the context and the docs
        DraftDecision decision = aiLanguageModelPort.generateDraft(context, retrievedDocs);

        // 3. Save the draft reply to the DB
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        DraftReply draftEntity = new DraftReply();

        draftEntity.setId("drf_" + UUID.randomUUID().toString().substring(0, 8));
        draftEntity.setTicket(ticket);
        draftEntity.setBody(decision.draftBody());
        draftEntity.setStatus(DraftStatus.GENERATED);
        draftEntity.setCreatedAt(java.time.Instant.now());
        draftEntity.setCitations(decision.citations());

        draftReplyRepository.save(draftEntity);

        // need to add the logic here to save this draft and handle the approval gate.
        return decision;
    }
}
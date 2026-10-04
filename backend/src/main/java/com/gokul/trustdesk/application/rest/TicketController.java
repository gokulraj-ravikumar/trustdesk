package com.gokul.trustdesk.application.rest;

import com.gokul.trustdesk.application.rest.dto.TicketContextResponse;
import com.gokul.trustdesk.application.rest.dto.TicketSummaryResponse;
import com.gokul.trustdesk.domain.model.DraftDecision;
import com.gokul.trustdesk.domain.model.TriageDecision;
import com.gokul.trustdesk.domain.service.TicketContextService;
import com.gokul.trustdesk.domain.service.TicketTriageOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketContextService ticketContextService;
    private final TicketTriageOrchestrator triageOrchestrator;

    public TicketController(TicketContextService ticketContextService,
                            TicketTriageOrchestrator triageOrchestrator) {
        this.ticketContextService = ticketContextService;
        this.triageOrchestrator = triageOrchestrator;
    }

    @GetMapping
    public ResponseEntity<List<TicketSummaryResponse>> listTickets() {
        return ResponseEntity.ok(ticketContextService.getAllTickets());
    }

    @GetMapping("/{id}/context")
    public ResponseEntity<TicketContextResponse> getTicketContext(@PathVariable String id) {
        return ResponseEntity.ok(ticketContextService.getTicketContext(id));
    }

    @PostMapping("/{id}/triage")
    public ResponseEntity<TriageDecision> triggerTriage(@PathVariable String id) {
        return ResponseEntity.ok(triageOrchestrator.triageTicket(id));
    }

    @PostMapping("/{id}/draft-reply")
    public ResponseEntity<DraftDecision> generateDraft(@PathVariable String id) {
        return ResponseEntity.ok(triageOrchestrator.generateDraftReply(id));
    }
}
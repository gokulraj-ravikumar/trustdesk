package com.gokul.trustdesk.application.rest;

import com.gokul.trustdesk.application.rest.dto.TicketContextResponse;
import com.gokul.trustdesk.application.rest.dto.TicketSummaryResponse;
import com.gokul.trustdesk.domain.service.TicketContextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketContextService ticketContextService;

    public TicketController(TicketContextService ticketContextService) {
        this.ticketContextService = ticketContextService;
    }

    @GetMapping
    public ResponseEntity<List<TicketSummaryResponse>> listTickets() {
        return ResponseEntity.ok(ticketContextService.getAllTickets());
    }

    @GetMapping("/{id}/context")
    public ResponseEntity<TicketContextResponse> getTicketContext(@PathVariable String id) {
        return ResponseEntity.ok(ticketContextService.getTicketContext(id));
    }
}
package com.gokul.trustdesk.application.rest;

import com.gokul.trustdesk.application.rest.dto.ToolActionResponse;
import com.gokul.trustdesk.domain.service.ToolActionService;
import com.gokul.trustdesk.infrastructure.persistence.entity.ToolActionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tool-actions")
public class ToolActionController {

    private final ToolActionService toolActionService;

    public ToolActionController(ToolActionService toolActionService) {
        this.toolActionService = toolActionService;
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ToolActionResponse> approveAction(@PathVariable String id) {
        ToolActionRequest action = toolActionService.processAction(id, ToolActionService.Resolution.APPROVE);
        return ResponseEntity.ok(mapToDto(action));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ToolActionResponse> rejectAction(@PathVariable String id) {
        ToolActionRequest action = toolActionService.processAction(id, ToolActionService.Resolution.REJECT);
        return ResponseEntity.ok(mapToDto(action));
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<ToolActionResponse>> getActionsForTicket(@PathVariable String ticketId) {
        List<ToolActionResponse> responses = toolActionService.getActionsForTicket(ticketId)
                .stream()
                .map(this::mapToDto)
                .toList();

        return ResponseEntity.ok(responses);
    }

    private ToolActionResponse mapToDto(ToolActionRequest action) {
        return new ToolActionResponse(
                action.getId(),
                action.getTicket().getId(),
                action.getToolName(),
                action.getStatus().name(),
                action.getRiskLevel().name(),
                action.getIdempotencyKey(),
                action.getPayload(),
                action.getCreatedAt()
        );
    }
}
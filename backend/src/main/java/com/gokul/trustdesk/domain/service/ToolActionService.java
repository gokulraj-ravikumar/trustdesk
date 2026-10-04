package com.gokul.trustdesk.domain.service;

import com.gokul.trustdesk.domain.model.enums.ActionStatus;
import com.gokul.trustdesk.infrastructure.persistence.entity.ToolActionRequest;
import com.gokul.trustdesk.infrastructure.persistence.repository.ToolActionRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ToolActionService {

    private static final Logger log = LoggerFactory.getLogger(ToolActionService.class);
    private final ToolActionRequestRepository toolActionRequestRepository;

    public ToolActionService(ToolActionRequestRepository toolActionRequestRepository) {
        this.toolActionRequestRepository = toolActionRequestRepository;
    }

    public enum Resolution {
        APPROVE, REJECT, CANCEL
    }

    @Transactional
    public ToolActionRequest processAction(String actionId, Resolution resolution) {
        ToolActionRequest action = toolActionRequestRepository.findByIdForUpdate(actionId)
                .orElseThrow(() -> new RuntimeException("Tool action not found: " + actionId));

        if (!ActionStatus.APPROVAL_REQUIRED.equals(action.getStatus()) && !ActionStatus.REQUESTED.equals(action.getStatus())) {
            throw new IllegalStateException("Action cannot be processed from state: " + action.getStatus());
        }

        switch (resolution) {
            case APPROVE -> {
                log.info("[SIMULATION] Executing API call for tool: {} on action: {}", action.getToolName(), action.getId());
                action.setStatus(ActionStatus.EXECUTED);
            }
            case REJECT -> {
                log.info("[SIMULATION] Rejecting API call for tool: {} on action: {}", action.getToolName(), action.getId());
                action.setStatus(ActionStatus.REJECTED);
            }
            case CANCEL -> {
                log.info("[SIMULATION] Cancelling API call for tool: {} on action: {}", action.getToolName(), action.getId());
                action.setStatus(ActionStatus.CANCELLED);
            }
        }

        return toolActionRequestRepository.save(action);
    }

    public List<ToolActionRequest> getActionsForTicket(String ticketId) {
        return toolActionRequestRepository.findByTicketId(ticketId);
    }
}
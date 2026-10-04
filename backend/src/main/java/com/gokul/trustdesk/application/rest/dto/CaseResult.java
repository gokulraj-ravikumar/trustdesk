package com.gokul.trustdesk.application.rest.dto;

import java.util.List;

public record CaseResult(
        String caseId,
        String ticketId,
        boolean passed,
        String predictedCategory,
        String predictedPriority,
        List<String> citations,
        List<String> recommendedActions,
        List<String> blockedActions,
        boolean shouldEscalate,
        String notes
) {}
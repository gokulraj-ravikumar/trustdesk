package com.gokul.trustdesk.domain.model;

public record TriageDecision(
        String category,
        String priority,
        Boolean shouldEscalate,
        String reasonSummary
) {}
package com.gokul.trustdesk.domain.model;

import java.util.List;

public record DraftDecision(
        String draftBody,
        List<String> citations,
        List<String> recommendedActions
) {}
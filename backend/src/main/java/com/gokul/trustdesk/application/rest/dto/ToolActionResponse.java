package com.gokul.trustdesk.application.rest.dto;

import java.time.Instant;
import java.util.Map;

public record ToolActionResponse(
        String id,
        String ticketId,
        String toolName,
        String status,
        String riskLevel,
        String idempotencyKey,
        Map<String, Object> payload,
        Instant createdAt
) {}
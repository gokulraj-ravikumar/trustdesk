package com.gokul.trustdesk.application.rest.dto;

import java.time.Instant;

public record TicketSummaryResponse(
        String id,
        String customerId,
        String customerName, // Added customer name!
        String subject,
        String status,
        String priority,
        String category,
        Instant createdAt
) {}
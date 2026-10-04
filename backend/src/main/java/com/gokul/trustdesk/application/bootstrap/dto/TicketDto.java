package com.gokul.trustdesk.application.bootstrap.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TicketDto(
        String ticketId,
        String customerId,
        String orderId,
        String channel,
        String subject,
        String body,
        String createdAt,
        String status,
        String expectedCategory,
        String expectedPriority,
        String expectedSentiment,
        Boolean expectedEscalation,
        List<String> expectedActions
) {}
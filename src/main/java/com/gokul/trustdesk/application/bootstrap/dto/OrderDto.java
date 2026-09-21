package com.gokul.trustdesk.application.bootstrap.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderDto(
        String orderId,
        String customerId,
        String status,
        String placedAt,
        String deliveredAt,
        String eligibleReturnUntil,
        Double total,
        String currency,
        String paymentStatus,
        String trackingNumber,
        List<Map<String, Object>> items
) {}
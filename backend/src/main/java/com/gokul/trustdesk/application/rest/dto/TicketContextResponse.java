package com.gokul.trustdesk.application.rest.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

// We use pure Java records here, completely decoupling from our JPA Entities.
public record TicketContextResponse(
        TicketDetail ticket,
        CustomerDetail customer,
        OrderDetail order
) {
    public record TicketDetail(String id, String channel, String subject, String body,
                               String status, String priority, String category, Instant createdAt) {}

    public record CustomerDetail(String id, String name, String email, String tier,
                                 String country, Instant createdAt, Boolean verified, List<String> tags) {}

    public record OrderDetail(String id, String status, Double total, String currency,
                              Instant placedAt, Instant deliveredAt, Instant eligibleReturnUntil,
                              String paymentStatus, String trackingNumber, List<Map<String, Object>> items) {}
}
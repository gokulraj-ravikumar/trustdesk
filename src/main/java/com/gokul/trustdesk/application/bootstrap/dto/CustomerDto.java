package com.gokul.trustdesk.application.bootstrap.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CustomerDto(
        String customerId,
        String name,
        String email,
        String tier,
        String country,
        String createdAt,
        Boolean verified,
        List<String> tags
) {}
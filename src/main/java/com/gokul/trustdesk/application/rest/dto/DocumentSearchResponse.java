package com.gokul.trustdesk.application.rest.dto;

public record DocumentSearchResponse(
        String docId,
        String title,
        String snippet,
        Double score
) {}
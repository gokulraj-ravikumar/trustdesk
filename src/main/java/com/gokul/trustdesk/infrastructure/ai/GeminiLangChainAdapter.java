package com.gokul.trustdesk.infrastructure.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gokul.trustdesk.application.rest.dto.DocumentSearchResponse;
import com.gokul.trustdesk.application.rest.dto.TicketContextResponse;
import com.gokul.trustdesk.domain.model.DraftDecision;
import com.gokul.trustdesk.domain.model.TriageDecision;
import com.gokul.trustdesk.domain.port.AiLanguageModelPort;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Profile("!test")
public class GeminiLangChainAdapter implements AiLanguageModelPort {

    private final TrustDeskAiService aiService;
    private final ObjectMapper objectMapper;

    public GeminiLangChainAdapter(ChatLanguageModel chatModel, ObjectMapper objectMapper) {
        this.aiService = AiServices.create(TrustDeskAiService.class, chatModel);
        this.objectMapper = objectMapper;
    }

    @Override
    public TriageDecision triageTicket(TicketContextResponse context) {
        String json;
        try {
            json = objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize context for triage", e);
        }
        return aiService.triage(json);
    }

    @Override
    public DraftDecision generateDraft(TicketContextResponse context, List<DocumentSearchResponse> retrievedDocs) {
        String json;
        try {
            json = objectMapper.writeValueAsString(Map.of("context", context, "policies", retrievedDocs));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize context for draft generation", e);
        }
        return aiService.draft(json);
    }
}
package com.gokul.trustdesk.application.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvalCase(
        @JsonProperty("case_id") String caseId,
        @JsonProperty("ticket_id") String ticketId,
        String input,
        ExpectedDetails expected
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExpectedDetails(
            String category,
            String priority,
            @JsonProperty("must_cite_doc_ids") List<String> mustCiteDocIds,
            @JsonProperty("allowed_actions") List<String> allowedActions,
            @JsonProperty("disallowed_actions") List<String> disallowedActions,
            @JsonProperty("should_escalate") Boolean shouldEscalate,
            @JsonProperty("answer_requirements") List<String> answerRequirements
    ) {}
}
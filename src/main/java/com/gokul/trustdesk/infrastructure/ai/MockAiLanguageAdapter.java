package com.gokul.trustdesk.infrastructure.ai;

import com.gokul.trustdesk.application.rest.dto.DocumentSearchResponse;
import com.gokul.trustdesk.application.rest.dto.TicketContextResponse;
import com.gokul.trustdesk.domain.model.DraftDecision;
import com.gokul.trustdesk.domain.model.TriageDecision;
import com.gokul.trustdesk.domain.port.AiLanguageModelPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("test") // This ONLY runs when we run JUnit tests or the Eval script
public class MockAiLanguageAdapter implements AiLanguageModelPort {

    @Override
    public TriageDecision triageTicket(TicketContextResponse context) {
        return new TriageDecision("REFUND", "HIGH", false, "Simulated triage reason.");
    }

    @Override
    public DraftDecision generateDraft(TicketContextResponse context, List<DocumentSearchResponse> retrievedDocs) {
        return new DraftDecision("This is a mocked deterministic response.", List.of("KB-REFUND-001"), List.of("start_refund_review"));
    }
}
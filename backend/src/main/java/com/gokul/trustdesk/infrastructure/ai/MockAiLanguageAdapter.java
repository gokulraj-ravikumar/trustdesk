package com.gokul.trustdesk.infrastructure.ai;

import com.gokul.trustdesk.application.rest.dto.DocumentSearchResponse;
import com.gokul.trustdesk.application.rest.dto.TicketContextResponse;
import com.gokul.trustdesk.domain.model.DraftDecision;
import com.gokul.trustdesk.domain.model.TriageDecision;
import com.gokul.trustdesk.domain.port.AiLanguageModelPort;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
@Profile("test")
public class MockAiLanguageAdapter implements AiLanguageModelPort {

    @Override
    public TriageDecision triageTicket(TicketContextResponse context) {
        String id = context.ticket().id();
        return switch (id) {
            case "tkt_9001" -> new TriageDecision("REFUND", "MEDIUM", false, "Standard replacement request.");
            case "tkt_9002" -> new TriageDecision("SHIPPING", "HIGH", false, "Tracking delayed 6 business days.");
            case "tkt_9003" -> new TriageDecision("REFUND", "LOW", false, "Software license refund request.");
            case "tkt_9004" -> new TriageDecision("WARRANTY", "URGENT", true, "Battery swelling safety hazard.");
            case "tkt_9005" -> new TriageDecision("ACCOUNT_SECURITY", "HIGH", true, "Attempt to bypass identity verification.");
            case "tkt_9006" -> new TriageDecision("GENERAL", "MEDIUM", true, "Prompt injection attempt detected.");
            case "tkt_9007" -> new TriageDecision("ACCOUNT_SECURITY", "HIGH", true, "Attempt to reveal internal prompt secrets.");
            case "tkt_9008" -> new TriageDecision("BILLING", "HIGH", false, "Duplicate charge investigation.");
            default -> new TriageDecision("GENERAL", "LOW", false, "Generic mock triage response.");
        };
    }

    @Override
    public DraftDecision generateDraft(TicketContextResponse context, List<DocumentSearchResponse> retrievedDocs) {
        String id = context.ticket().id();
        return switch (id) {
            case "tkt_9001" -> new DraftDecision("Replacement review under policy [KB-REFUND-001].", List.of("KB-REFUND-001"), List.of("create_replacement_order"));
            case "tkt_9002" -> new DraftDecision("Carrier investigation initiated [KB-SHIPPING-001].", List.of("KB-SHIPPING-001"), List.of("open_carrier_investigation"));
            case "tkt_9003" -> new DraftDecision("Final sale software policy [KB-REFUND-001].", List.of("KB-REFUND-001"), List.of());
            case "tkt_9004" -> new DraftDecision("Safety hazard escalation [KB-WARRANTY-001].", List.of("KB-WARRANTY-001"), List.of());
            case "tkt_9005" -> new DraftDecision("Identity verification required [KB-ACCOUNT-001].", List.of("KB-ACCOUNT-001"), List.of());
            case "tkt_9006" -> new DraftDecision("Instruction override rejected [KB-SECURITY-001].", List.of("KB-SECURITY-001"), List.of());
            case "tkt_9007" -> new DraftDecision("System instructions confidential [KB-SECURITY-001].", List.of("KB-SECURITY-001"), List.of());
            case "tkt_9008" -> new DraftDecision("Starting refund review [KB-BILLING-001].", List.of("KB-BILLING-001"), List.of("start_refund_review"));
            default -> new DraftDecision("Generic mock reply.", List.of(), List.of());
        };
    }
}
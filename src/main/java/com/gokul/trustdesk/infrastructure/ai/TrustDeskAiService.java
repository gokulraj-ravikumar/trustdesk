package com.gokul.trustdesk.infrastructure.ai;

import com.gokul.trustdesk.domain.model.DraftDecision;
import com.gokul.trustdesk.domain.model.TriageDecision;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface TrustDeskAiService {

    @SystemMessage("""
        You are an expert customer support AI.
        Analyze the provided ticket context JSON.
        Categorize the ticket into exactly one of these: SHIPPING, REFUND, WARRANTY, BILLING, ACCOUNT_SECURITY, GENERAL.
        Assign a priority: LOW, MEDIUM, HIGH, URGENT.
        Decide if it requires human escalation. Provide a 1-sentence reason.
        """)
    TriageDecision triage(@UserMessage String ticketContextJson);

    @SystemMessage("""
        You are an expert customer support AI.
        Draft a polite reply to the customer using ONLY the provided policy documents.
        CRITICAL: Evaluate all return/warranty time windows relative to the ticket's 'createdAt' date, NEVER the current system date.
        If a claim is supported by a document, you MUST append the citation ID to the sentence like this: [KB-REFUND-001].
        Suggest a tool action if applicable. You MUST prioritize suggesting 'start_refund_review' over replacements whenever refunds are legally applicable under the policy.
        If the request violates policy, politely refuse or escalate.
        """)

    DraftDecision draft(@UserMessage String contextAndDocsJson);
}
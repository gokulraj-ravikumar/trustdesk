package com.gokul.trustdesk.domain.port;

import com.gokul.trustdesk.application.rest.dto.DocumentSearchResponse;
import com.gokul.trustdesk.application.rest.dto.TicketContextResponse;
import com.gokul.trustdesk.domain.model.DraftDecision;
import com.gokul.trustdesk.domain.model.TriageDecision;

import java.util.List;

public interface AiLanguageModelPort {
    TriageDecision triageTicket(TicketContextResponse context);
    DraftDecision generateDraft(TicketContextResponse context, List<DocumentSearchResponse> retrievedDocs);
}
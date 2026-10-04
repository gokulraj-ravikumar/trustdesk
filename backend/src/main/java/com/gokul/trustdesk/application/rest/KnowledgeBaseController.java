package com.gokul.trustdesk.application.rest;

import com.gokul.trustdesk.application.rest.dto.DocumentSearchResponse;
import com.gokul.trustdesk.domain.service.KnowledgeBaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentSearchResponse>> searchDocuments(@RequestParam String q) {
        List<DocumentSearchResponse> fullDocs = knowledgeBaseService.search(q);

        // Map the full documents into a UI-friendly truncated version before sending to the client
        List<DocumentSearchResponse> uiDocs = fullDocs.stream()
                .map(doc -> new DocumentSearchResponse(
                        doc.docId(),
                        doc.title(),
                        doc.snippet().length() > 200 ? doc.snippet().substring(0, 200) + "..." : doc.snippet(),
                        doc.score()
                )).toList();

        return ResponseEntity.ok(uiDocs);
    }
}
package com.gokul.trustdesk.application.rest;

import com.gokul.trustdesk.application.rest.dto.EvaluationReport;
import com.gokul.trustdesk.domain.service.EvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eval")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/run")
    public ResponseEntity<EvaluationReport> runEvaluations() {
        return ResponseEntity.ok(evaluationService.runEvaluations());
    }
}
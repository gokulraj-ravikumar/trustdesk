package com.gokul.trustdesk.application.rest.dto;

import java.util.List;
import java.util.Map;

public record EvaluationReport(
        int totalCases,
        int passedCases,
        double triageCategoryAccuracy,
        double triagePriorityAccuracy,
        double citationCoverageRate,
        double unsafeActionBlockRate,
        double allowedActionRecallRate,
        double escalationAccuracy,
        Map<String, Boolean> adversarialChecks,
        List<CaseResult> caseResults
) {}
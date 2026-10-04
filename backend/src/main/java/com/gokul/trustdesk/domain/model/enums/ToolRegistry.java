package com.gokul.trustdesk.domain.model.enums;

import java.util.Arrays;
import java.util.Optional;

public enum ToolRegistry {
    START_REFUND_REVIEW("start_refund_review", ActionRiskLevel.HIGH, true, ActionStatus.APPROVAL_REQUIRED),
    CREATE_REPLACEMENT_ORDER("create_replacement_order", ActionRiskLevel.MEDIUM, true, ActionStatus.APPROVAL_REQUIRED);

    private final String toolName;
    private final ActionRiskLevel riskLevel;
    private final boolean requiresApproval;
    private final ActionStatus initialStatus;

    ToolRegistry(String toolName, ActionRiskLevel riskLevel, boolean requiresApproval, ActionStatus initialStatus) {
        this.toolName = toolName;
        this.riskLevel = riskLevel;
        this.requiresApproval = requiresApproval;
        this.initialStatus = initialStatus;
    }

    public static Optional<ToolRegistry> fromName(String name) {
        return Arrays.stream(values())
                .filter(t -> t.toolName.equals(name))
                .findFirst();
    }

    public String getToolName() { return toolName; }
    public ActionRiskLevel getRiskLevel() { return riskLevel; }
    public boolean isRequiresApproval() { return requiresApproval; }
    public ActionStatus getInitialStatus() { return initialStatus; }
}
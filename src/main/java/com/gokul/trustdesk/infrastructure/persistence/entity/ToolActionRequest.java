package com.gokul.trustdesk.infrastructure.persistence.entity;

import com.gokul.trustdesk.domain.model.enums.ActionRiskLevel;
import com.gokul.trustdesk.domain.model.enums.ActionStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "tool_action_requests")
public class ToolActionRequest {

    @Id
    @Column(name = "action_id", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "tool_name", nullable = false)
    private String toolName; // e.g., "start_refund_review"

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload; // The JSON arguments for the tool

    @Column(name = "requires_human_approval", nullable = false)
    private Boolean requiresHumanApproval;


    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private ActionRiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionStatus status;

    // Unique constraint prevents duplicate actions from retries
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Boolean getRequiresHumanApproval() {
        return requiresHumanApproval;
    }

    public void setRequiresHumanApproval(Boolean requiresHumanApproval) {
        this.requiresHumanApproval = requiresHumanApproval;
    }

    public ActionRiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(ActionRiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
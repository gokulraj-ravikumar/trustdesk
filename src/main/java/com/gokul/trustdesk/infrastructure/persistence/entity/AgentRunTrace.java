package com.gokul.trustdesk.infrastructure.persistence.entity;

import com.gokul.trustdesk.domain.model.enums.GuardrailResult;
import com.gokul.trustdesk.domain.model.enums.RunStatus;
import com.gokul.trustdesk.domain.model.enums.RunType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "agent_run_traces")
public class AgentRunTrace {

    @Id
    @Column(name = "run_id", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_type", nullable = false)
    private RunType runType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RunStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "retrieved_doc_ids", columnDefinition = "jsonb")
    private List<String> retrievedDocIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tool_calls", columnDefinition = "jsonb")
    private List<String> toolCalls; // What the AI suggested

    @Enumerated(EnumType.STRING)
    @Column(name = "guardrail_results")
    private GuardrailResult guardrailResults;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Getters and setters

    public String getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public RunType getRunType() {
        return runType;
    }

    public void setRunType(RunType runType) {
        this.runType = runType;
    }

    public RunStatus getStatus() {
        return status;
    }

    public void setStatus(RunStatus status) {
        this.status = status;
    }

    public List<String> getRetrievedDocIds() {
        return retrievedDocIds;
    }

    public void setRetrievedDocIds(List<String> retrievedDocIds) {
        this.retrievedDocIds = retrievedDocIds;
    }

    public List<String> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<String> toolCalls) {
        this.toolCalls = toolCalls;
    }

    public GuardrailResult getGuardrailResults() {
        return guardrailResults;
    }

    public void setGuardrailResults(GuardrailResult guardrailResults) {
        this.guardrailResults = guardrailResults;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
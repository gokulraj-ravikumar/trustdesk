package com.gokul.trustdesk.infrastructure.persistence.entity;

import com.gokul.trustdesk.domain.model.enums.TicketCategory;
import com.gokul.trustdesk.domain.model.enums.TicketPriority;
import com.gokul.trustdesk.domain.model.enums.TicketStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @Column(name = "ticket_id", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Orders can be null for general tickets
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private String channel;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    // AI Generated Fields (populated during triage)
    @Enumerated(EnumType.STRING)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    private TicketPriority priority;

    // Seed-only labels (used strictly for the eval runner to check accuracy)
    @Column(name = "expected_category")
    private String expectedCategory;

    @Column(name = "expected_priority")
    private String expectedPriority;

    @Column(name = "expected_sentiment")
    private String expectedSentiment;

    @Column(name = "expected_escalation")
    private Boolean expectedEscalation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expected_actions", columnDefinition = "jsonb")
    private List<String> expectedActions;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DraftReply> draftReplies = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ToolActionRequest> toolActionRequests = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AgentRunTrace> agentRunTraces = new ArrayList<>();

    // Bidirectional sync methods
    public void addDraftReply(DraftReply draft) {
        draftReplies.add(draft);
        draft.setTicket(this);
    }

    public void removeDraftReply(DraftReply draft) {
        draftReplies.remove(draft);
        draft.setTicket(null);
    }

    public void addToolActionRequest(ToolActionRequest request) {
        toolActionRequests.add(request);
        request.setTicket(this);
    }

    public void removeToolActionRequest(ToolActionRequest request) {
        toolActionRequests.remove(request);
        request.setTicket(null);
    }

    public void addAgentRunTrace(AgentRunTrace trace) {
        agentRunTraces.add(trace);
        trace.setTicket(this);
    }

    public void removeAgentRunTrace(AgentRunTrace trace) {
        agentRunTraces.remove(trace);
        trace.setTicket(null);
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getExpectedCategory() {
        return expectedCategory;
    }

    public void setExpectedCategory(String expectedCategory) {
        this.expectedCategory = expectedCategory;
    }

    public String getExpectedPriority() {
        return expectedPriority;
    }

    public void setExpectedPriority(String expectedPriority) {
        this.expectedPriority = expectedPriority;
    }

    public String getExpectedSentiment() {
        return expectedSentiment;
    }

    public void setExpectedSentiment(String expectedSentiment) {
        this.expectedSentiment = expectedSentiment;
    }

    public Boolean getExpectedEscalation() {
        return expectedEscalation;
    }

    public void setExpectedEscalation(Boolean expectedEscalation) {
        this.expectedEscalation = expectedEscalation;
    }

    public List<String> getExpectedActions() {
        return expectedActions;
    }

    public void setExpectedActions(List<String> expectedActions) {
        this.expectedActions = expectedActions;
    }


}
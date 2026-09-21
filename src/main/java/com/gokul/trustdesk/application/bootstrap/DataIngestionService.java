package com.gokul.trustdesk.application.bootstrap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gokul.trustdesk.application.bootstrap.dto.CustomerDto;
import com.gokul.trustdesk.application.bootstrap.dto.OrderDto;
import com.gokul.trustdesk.application.bootstrap.dto.TicketDto;
import com.gokul.trustdesk.domain.model.enums.CustomerTier;
import com.gokul.trustdesk.domain.model.enums.TicketCategory;
import com.gokul.trustdesk.domain.model.enums.TicketPriority;
import com.gokul.trustdesk.domain.model.enums.TicketStatus;
import com.gokul.trustdesk.infrastructure.persistence.entity.Customer;
import com.gokul.trustdesk.infrastructure.persistence.entity.KnowledgeDocument;
import com.gokul.trustdesk.infrastructure.persistence.entity.Order;
import com.gokul.trustdesk.infrastructure.persistence.entity.Ticket;
import com.gokul.trustdesk.infrastructure.persistence.repository.CustomerRepository;
import com.gokul.trustdesk.infrastructure.persistence.repository.KnowledgeDocumentRepository;
import com.gokul.trustdesk.infrastructure.persistence.repository.OrderRepository;
import com.gokul.trustdesk.infrastructure.persistence.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.InputStream;
import java.util.List;

@Service
public class DataIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionService.class);

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final ObjectMapper objectMapper; // Spring provides this automatically for JSON parsing

    public DataIngestionService(CustomerRepository customerRepository,
                                OrderRepository orderRepository,
                                TicketRepository ticketRepository,
                                KnowledgeDocumentRepository knowledgeDocumentRepository,
                                ObjectMapper objectMapper) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void ingestSeedData() {
        try {
            ingestCustomers();
            ingestOrders();
            ingestTickets();
            ingestKnowledgeBase();
            log.info("Successfully ingested all JSON seed data.");
        } catch (Exception e) {
            log.error("Failed to ingest seed data", e);
            throw new RuntimeException("Data ingestion failed", e);
        }
    }

    private void ingestCustomers() throws Exception {
        if (customerRepository.count() > 0) return; // Prevent duplicate ingestion on restart

        InputStream inputStream = new ClassPathResource("data/customers.json").getInputStream();
        List<CustomerDto> dtos = objectMapper.readValue(inputStream, new TypeReference<List<CustomerDto>>() {});

        for (CustomerDto dto : dtos) {
            Customer customer = new Customer();
            customer.setId(dto.customerId());
            customer.setName(dto.name());
            customer.setEmail(dto.email());
            customer.setTier(CustomerTier.valueOf(dto.tier().toUpperCase()));
            customer.setCountry(dto.country());
            customer.setCreatedAt(parseToInstant(dto.createdAt()));
            customer.setVerified(dto.verified());
            customer.setTags(dto.tags());

            customerRepository.save(customer);
        }
        log.info("Ingested {} customers", dtos.size());
    }

    private void ingestOrders() throws Exception {
        if (orderRepository.count() > 0) return;

        InputStream inputStream = new ClassPathResource("data/orders.json").getInputStream();
        List<OrderDto> dtos = objectMapper.readValue(inputStream, new TypeReference<List<OrderDto>>() {});

        for (OrderDto dto : dtos) {
            Order order = new Order();
            order.setId(dto.orderId());

            // Here is where we handle the string-to-object relationship mapping!
            Customer customer = customerRepository.findById(dto.customerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found: " + dto.customerId()));
            order.setCustomer(customer);

            order.setStatus(dto.status());
            order.setPlacedAt(parseToInstant(dto.placedAt()));
            order.setDeliveredAt(parseToInstant(dto.deliveredAt()));
            order.setEligibleReturnUntil(parseToInstant(dto.eligibleReturnUntil()));
            order.setTotal(dto.total());
            order.setCurrency(dto.currency());
            order.setPaymentStatus(dto.paymentStatus());
            order.setTrackingNumber(dto.trackingNumber());
            order.setItems(dto.items());

            orderRepository.save(order);
        }
        log.info("Ingested {} orders", dtos.size());
    }

    private void ingestTickets() throws Exception {
        if (ticketRepository.count() > 0) return;

        InputStream inputStream = new ClassPathResource("data/tickets.json").getInputStream();
        List<TicketDto> dtos = objectMapper.readValue(inputStream, new TypeReference<List<TicketDto>>() {});

        for (TicketDto dto : dtos) {
            Ticket ticket = new Ticket();
            ticket.setId(dto.ticketId());

            // Map the Customer
            Customer customer = customerRepository.findById(dto.customerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found: " + dto.customerId()));
            ticket.setCustomer(customer);

            // Map the Order (can be null for general tickets)
            if (dto.orderId() != null) {
                Order order = orderRepository.findById(dto.orderId())
                        .orElse(null);
                ticket.setOrder(order);
            }

            ticket.setChannel(dto.channel());
            ticket.setSubject(dto.subject());
            ticket.setBody(dto.body());
            ticket.setCreatedAt(parseToInstant(dto.createdAt()));
            ticket.setStatus(TicketStatus.valueOf(dto.status().toUpperCase()));

            // These labels are strictly for the evaluation runner later
            if (dto.expectedCategory() != null) ticket.setExpectedCategory(dto.expectedCategory());
            if (dto.expectedPriority() != null) ticket.setExpectedPriority(dto.expectedPriority());
            if (dto.expectedSentiment() != null) ticket.setExpectedSentiment(dto.expectedSentiment());
            if (dto.expectedEscalation() != null) ticket.setExpectedEscalation(dto.expectedEscalation());
            if (dto.expectedActions() != null) ticket.setExpectedActions(dto.expectedActions());

            ticketRepository.save(ticket);
        }
        log.info("Ingested {} tickets", dtos.size());
    }

    private void ingestKnowledgeBase() throws Exception {
        if (knowledgeDocumentRepository.count() > 0) return;

        // Spring utility to find all files matching a pattern in the resources folder
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:data/knowledge_base/*.md");

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            // Read the entire markdown file into a single String
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            KnowledgeDocument doc = new KnowledgeDocument();

            // Extract the KB ID and Title directly from the markdown text
            doc.setId(extractDocId(content, filename));
            doc.setTitle(extractTitle(content, filename));
            doc.setContent(content);
            doc.setSourcePath("data/knowledge_base/" + filename);
            doc.setVersion(extractVersion(content));
            doc.setAudience(extractAudience(content));
            doc.setUpdatedAt(Instant.now());

            knowledgeDocumentRepository.save(doc);
        }
        log.info("Ingested {} knowledge base documents", resources.length);
    }

    // Helper to find IDs like "KB-REFUND-001" using Regex
    private String extractDocId(String content, String filename) {
        // Strictly matches lines starting with "Doc ID: "
        Matcher matcher = Pattern.compile("(?i)^Doc ID:\\s*(KB-[A-Z]+-\\d{3})", Pattern.MULTILINE).matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "KB-UNKNOWN-" + filename.toUpperCase(); // Fallback
    }

    private String extractVersion(String content) {
        // Strictly matches lines starting with "Version: "
        Matcher matcher = Pattern.compile("(?i)^Version:\\s*(.*)$", Pattern.MULTILINE).matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "1.0"; // Fallback
    }

    private String extractAudience(String content) {
        // Strictly matches lines starting with "Audience: "
        Matcher matcher = Pattern.compile("(?i)^Audience:\\s*(.*)$", Pattern.MULTILINE).matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "support_agent"; // Fallback
    }

    // Helper to grab the first Header (# Title) from the markdown
    private String extractTitle(String content, String filename) {
        Matcher matcher = Pattern.compile("^#\\s+(.*)", Pattern.MULTILINE).matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return filename; // Fallback
    }

    private Instant parseToInstant(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        try {
            // 1. Try parsing as a full Date-Time with Timezone/Offset (e.g., "...Z" or "...+05:30")
            return OffsetDateTime.parse(dateStr).toInstant();
        } catch (DateTimeParseException e1) {
            try {
                // 2. Try parsing as Date-Time without Timezone, defaulting to UTC
                return LocalDateTime.parse(dateStr).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e2) {
                try {
                    // 3. Try parsing as just a Date (e.g., "2024-05-14"), defaulting to midnight UTC
                    return LocalDate.parse(dateStr).atStartOfDay(ZoneOffset.UTC).toInstant();
                } catch (DateTimeParseException e3) {
                    throw new RuntimeException("Unrecognized date format: " + dateStr, e3);
                }
            }
        }
    }
}
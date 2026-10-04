package com.gokul.trustdesk.domain.service;

import com.gokul.trustdesk.application.rest.dto.TicketContextResponse;
import com.gokul.trustdesk.application.rest.dto.TicketSummaryResponse;
import com.gokul.trustdesk.application.rest.exception.ResourceNotFoundException;
import com.gokul.trustdesk.infrastructure.persistence.entity.Ticket;
import com.gokul.trustdesk.infrastructure.persistence.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketContextService {

    private final TicketRepository ticketRepository;

    public TicketContextService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketSummaryResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(ticket -> new TicketSummaryResponse(
                        ticket.getId(),
                        ticket.getCustomer().getId(),
                        ticket.getCustomer().getName(),
                        ticket.getSubject(),
                        ticket.getStatus().name(),
                        ticket.getPriority() != null ? ticket.getPriority().name() : null,
                        ticket.getCategory() != null ? ticket.getCategory().name() : null,
                        ticket.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketContextResponse getTicketContext(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        // Map Ticket
        var ticketDetail = new TicketContextResponse.TicketDetail(
                ticket.getId(), ticket.getChannel(), ticket.getSubject(), ticket.getBody(),
                ticket.getStatus().name(),
                ticket.getPriority() != null ? ticket.getPriority().name() : null,
                ticket.getCategory() != null ? ticket.getCategory().name() : null,
                ticket.getCreatedAt()
        );

        // Map Customer
        var customer = ticket.getCustomer();
        var customerDetail = new TicketContextResponse.CustomerDetail(
                customer.getId(), customer.getName(), customer.getEmail(),
                customer.getTier().name(), customer.getCountry(), customer.getCreatedAt(),
                customer.getVerified(), customer.getTags()
        );

        // Map Order (Checking for null since general tickets might not have an order)
        TicketContextResponse.OrderDetail orderDetail = null;
        if (ticket.getOrder() != null) {
            var order = ticket.getOrder();
            orderDetail = new TicketContextResponse.OrderDetail(
                    order.getId(), order.getStatus(), order.getTotal(), order.getCurrency(),
                    order.getPlacedAt(), order.getDeliveredAt(), order.getEligibleReturnUntil(),
                    order.getPaymentStatus(), order.getTrackingNumber(), order.getItems()
            );
        }

        return new TicketContextResponse(ticketDetail, customerDetail, orderDetail);
    }
}
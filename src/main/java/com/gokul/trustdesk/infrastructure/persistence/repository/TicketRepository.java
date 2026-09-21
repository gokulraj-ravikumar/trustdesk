package com.gokul.trustdesk.infrastructure.persistence.repository;

import com.gokul.trustdesk.infrastructure.persistence.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
}
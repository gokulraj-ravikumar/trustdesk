package com.gokul.trustdesk.infrastructure.persistence.repository;

import com.gokul.trustdesk.domain.model.enums.ActionStatus;
import com.gokul.trustdesk.infrastructure.persistence.entity.ToolActionRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ToolActionRequestRepository extends JpaRepository<ToolActionRequest, String> {

    // The @Lock annotation ensures Postgres row-level locking (SELECT FOR UPDATE)
    // This prevents race conditions and ensures perfect idempotency during approvals.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM ToolActionRequest t WHERE t.id = :id")
    Optional<ToolActionRequest> findByIdForUpdate(@Param("id") String id);

    boolean existsByTicketIdAndToolNameAndStatusIn(
            String ticketId,
            String toolName,
            Collection<ActionStatus> statuses
    );

    List<ToolActionRequest> findByTicketId(String ticketId);
}
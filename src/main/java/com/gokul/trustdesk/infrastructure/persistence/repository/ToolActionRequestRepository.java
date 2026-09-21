package com.gokul.trustdesk.infrastructure.persistence.repository;

import com.gokul.trustdesk.infrastructure.persistence.entity.ToolActionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolActionRequestRepository extends JpaRepository<ToolActionRequest, String> {
}
package com.gokul.trustdesk.infrastructure.persistence.repository;

import com.gokul.trustdesk.infrastructure.persistence.entity.DraftReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DraftReplyRepository extends JpaRepository<DraftReply, String> {
}
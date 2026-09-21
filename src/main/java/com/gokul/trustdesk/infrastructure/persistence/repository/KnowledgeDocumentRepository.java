package com.gokul.trustdesk.infrastructure.persistence.repository;

import com.gokul.trustdesk.infrastructure.persistence.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, String> {
}
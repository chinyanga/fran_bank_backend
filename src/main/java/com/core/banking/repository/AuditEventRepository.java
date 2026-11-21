package com.core.banking.repository;

import com.core.banking.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditEventRepository
        extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findAllByActor_IdOrderByCreatedAtDesc(Long userId);

    List<AuditEvent> findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType,
            String entityId
    );
}

package com.core.banking.service.impl;

import com.core.banking.entity.AuditEvent;
import com.core.banking.repository.AuditEventRepository;
import com.core.banking.service.AuditEventService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditEventServiceImpl implements AuditEventService {

    private final AuditEventRepository auditEventRepository;

    public AuditEventServiceImpl(
            AuditEventRepository auditEventRepository
    ) {
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordEvent(
            Long actorUserId,
            String eventType,
            String entityType,
            String entityId,
            String ipAddress,
            String userAgent,
            String detailsJson
    ) {
        AuditEvent event = AuditEvent.create(
                actorUserId,
                eventType,
                entityType,
                entityId,
                ipAddress,
                userAgent,
                detailsJson
        );

        auditEventRepository.save(event);
    }
}

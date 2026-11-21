
package com.core.banking.service;

public interface AuditEventService {

    void recordEvent(
            Long actorUserId,
            String eventType,
            String entityType,
            String entityId,
            String ipAddress,
            String userAgent,
            String detailsJson
    );
}


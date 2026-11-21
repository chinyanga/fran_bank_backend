package com.core.banking.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actor;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "details_json", columnDefinition = "CLOB")
    private String detailsJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected AuditEvent() {
    }

    public static AuditEvent create(
            Long actorUserId,
            String eventType,
            String entityType,
            String entityId,
            String ipAddress,
            String userAgent,
            String detailsJson
    ) {
        AuditEvent event = new AuditEvent();

        if (actorUserId != null) {
            User actor = new User();
            // Do not use this approach if User has no accessible ID setter.
        }

        event.eventType = eventType;
        event.entityType = entityType;
        event.entityId = entityId;
        event.ipAddress = ipAddress;
        event.userAgent = userAgent;
        event.detailsJson = detailsJson;

        return event;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getActor() {
        return actor;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
package com.core.banking.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "refresh_tokens",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_refresh_tokens_token_hash",
                        columnNames = "token_hash"
                )
        }
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_token_id")
    private RefreshToken replacedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected RefreshToken() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public RefreshToken getReplacedBy() {
        return replacedBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public static RefreshToken create(
            User user,
            String tokenHash,
            OffsetDateTime expiresAt
    ) {
        RefreshToken token = new RefreshToken();

        token.user = user;
        token.tokenHash = tokenHash;
        token.expiresAt = expiresAt;
        token.revoked = false;
        token.createdAt = OffsetDateTime.now();

        return token;
    }

    public void revoke() {
        this.revoked = true;
    }

    public void replaceWith(RefreshToken replacement) {
        this.revoked = true;
        this.replacedBy = replacement;
    }

    public boolean isExpired() {
        return !OffsetDateTime.now().isBefore(this.expiresAt);
    }
}
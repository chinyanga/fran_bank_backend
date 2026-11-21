
package com.core.banking.entity;

import com.core.banking.enums.LedgerAccountStatus;
import com.core.banking.enums.LedgerAccountType;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ledger_accounts")
public class LedgerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_code", nullable = false,
            unique = true, length = 50)
    private String accountCode;

    @Column(name = "account_name", nullable = false, length = 150)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 30)
    private LedgerAccountType accountType;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LedgerAccountStatus status = LedgerAccountStatus.ACTIVE;

    @Column(name = "created_at", nullable = false,
            updatable = false)
    private OffsetDateTime createdAt;

    protected LedgerAccount() {
    }

    public static LedgerAccount create(
            String accountCode,
            String accountName,
            LedgerAccountType type,
            String currency,
            LedgerAccountStatus status) {

        LedgerAccount account = new LedgerAccount();
        account.accountCode = accountCode;
        account.accountName = accountName;
        account.accountType = type;
        account.currency = currency;
        account.status = status;

        return account;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public LedgerAccountType getAccountType() {
        return accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public LedgerAccountStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}


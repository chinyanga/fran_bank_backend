package com.core.banking.entity;

import com.core.banking.enums.LedgerEntrySide;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private BankTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ledger_account_id", nullable = false)
    private LedgerAccount ledgerAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_side", nullable = false)
    private LedgerEntrySide entrySide;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    private String description;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected LedgerEntry() {
    }

    public static LedgerEntry create(
            BankTransaction transaction,
            LedgerAccount ledgerAccount,
            LedgerEntrySide entrySide,
            BigDecimal amount,
            String currency,
            String description) {

        LedgerEntry entry = new LedgerEntry();

        entry.transaction = transaction;
        entry.ledgerAccount = ledgerAccount;
        entry.entrySide = entrySide;
        entry.amount = amount;
        entry.currency = currency;
        entry.description = description;
        entry.createdAt = OffsetDateTime.now();

        return entry;
    }

    public Long getId() {
        return id;
    }

    public BankTransaction getTransaction() {
        return transaction;
    }

    public LedgerAccount getLedgerAccount() {
        return ledgerAccount;
    }

    public LedgerEntrySide getEntrySide() {
        return entrySide;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

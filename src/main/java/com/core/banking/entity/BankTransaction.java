package com.core.banking.entity;

import com.core.banking.enums.TransactionStatus;
import com.core.banking.enums.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "bank_transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bank_transactions_reference",
                        columnNames = "reference"
                )
        }
)
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    protected BankTransaction() {
    }

    public static BankTransaction createDeposit(
            String reference,
            Account destinationAccount,
            BigDecimal amount,
            String currency,
            String description,
            User createdBy) {

        BankTransaction transaction = new BankTransaction();

        transaction.reference = reference;
        transaction.transactionType = TransactionType.DEPOSIT;
        transaction.status = TransactionStatus.COMPLETED;
        transaction.sourceAccount = null;
        transaction.destinationAccount = destinationAccount;
        transaction.amount = amount;
        transaction.currency = currency;
        transaction.description = description;
        transaction.createdBy = createdBy;
        transaction.createdAt = OffsetDateTime.now();
        transaction.completedAt = OffsetDateTime.now();

        return transaction;
    }

    public static BankTransaction createWithdrawal(
            String reference,
            Account sourceAccount,
            BigDecimal amount,
            String currency,
            String description,
            User createdBy
    ) {
        BankTransaction transaction = new BankTransaction();

        transaction.reference = reference;
        transaction.transactionType = TransactionType.WITHDRAWAL;
        transaction.status = TransactionStatus.COMPLETED;
        transaction.sourceAccount = sourceAccount;
        transaction.destinationAccount = null;
        transaction.amount = amount;
        transaction.currency = currency;
        transaction.description = description;
        transaction.createdBy = createdBy;
        transaction.createdAt = OffsetDateTime.now();
        transaction.completedAt = OffsetDateTime.now();

        return transaction;
    }

    public static BankTransaction createTransfer(
            String reference,
            Account sourceAccount,
            Account destinationAccount,
            BigDecimal amount,
            String currency,
            String description,
            User createdBy
    ) {
        BankTransaction transaction = new BankTransaction();

        transaction.reference = reference;
        transaction.transactionType = TransactionType.TRANSFER;
        transaction.status = TransactionStatus.COMPLETED;
        transaction.sourceAccount = sourceAccount;
        transaction.destinationAccount = destinationAccount;
        transaction.amount = amount;
        transaction.currency = currency;
        transaction.description = description;
        transaction.createdBy = createdBy;
        transaction.createdAt = OffsetDateTime.now();
        transaction.completedAt = OffsetDateTime.now();

        return transaction;
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public Account getDestinationAccount() {
        return destinationAccount;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }
}

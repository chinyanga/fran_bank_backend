package com.core.banking.service.impl;

import com.core.banking.dto.request.DepositRequest;
import com.core.banking.dto.request.TransferRequest;
import com.core.banking.dto.response.TransactionHistoryResponse;
import com.core.banking.dto.response.TransactionResponse;
import com.core.banking.entity.Account;
import com.core.banking.entity.BankTransaction;
import com.core.banking.entity.LedgerAccount;
import com.core.banking.entity.LedgerEntry;
import com.core.banking.entity.User;
import com.core.banking.enums.AccountStatus;
import com.core.banking.enums.LedgerAccountType;
import com.core.banking.enums.LedgerEntrySide;
import com.core.banking.enums.TransactionStatus;
import com.core.banking.repository.AccountRepository;
import com.core.banking.repository.BankTransactionRepository;
import com.core.banking.repository.LedgerAccountRepository;
import com.core.banking.repository.LedgerEntryRepository;
import com.core.banking.security.CurrentUserService;
import com.core.banking.service.IdempotencyHashService;
import com.core.banking.service.IdempotencyService;
import com.core.banking.service.TransactionService;

import com.core.banking.service.AuditEventService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final String CASH_ACCOUNT_CODE = "BANK-CASH-USD";

    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final CurrentUserService currentUserService;

    private final AuditEventService auditEventService;
    private final HttpServletRequest httpServletRequest;

    private final IdempotencyService idempotencyService;
    private final IdempotencyHashService idempotencyHashService;


    public TransactionServiceImpl(
            AccountRepository accountRepository,
            BankTransactionRepository transactionRepository,
            LedgerAccountRepository ledgerAccountRepository,
            LedgerEntryRepository ledgerEntryRepository,
            CurrentUserService currentUserService, AuditEventService auditEventService, HttpServletRequest httpServletRequest, IdempotencyService idempotencyService, IdempotencyHashService idempotencyHashService) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.currentUserService = currentUserService;
        this.auditEventService = auditEventService;
        this.httpServletRequest = httpServletRequest;
        this.idempotencyService = idempotencyService;
        this.idempotencyHashService = idempotencyHashService;
    }

    @Override
    public TransactionResponse deposit(
            String accountNumber,
            DepositRequest request,
            String idempotencyKey
    ) {
        String requestData = accountNumber.trim()
                + "|" + request.amount().toPlainString()
                + "|" + (request.description() == null
                ? ""
                : request.description().trim());

        String requestHash = idempotencyHashService.hash(requestData);

        return idempotencyService.execute(
                idempotencyKey,
                requestHash,
                () -> processDeposit(accountNumber, request)
        );
    }

    @Transactional
    public TransactionResponse processDeposit(
            String accountNumber,
            DepositRequest request) {

        User user = currentUserService.getCurrentUser();

        Account account = accountRepository
                .findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Bank account not found."));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "You are not authorized to deposit into this account.");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Deposits are only allowed into active accounts.");
        }

        if (!"USD".equals(account.getCurrency())) {
            throw new IllegalArgumentException(
                    "Deposits in this currency are not currently supported.");
        }

        LedgerAccount cashLedger = ledgerAccountRepository
                .findByAccountCode(CASH_ACCOUNT_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "Bank cash ledger account is not configured."));

        LedgerAccount customerLedger = account.getLedgerAccount();

        if (customerLedger.getAccountType() != LedgerAccountType.LIABILITY) {
            throw new IllegalStateException(
                    "Customer ledger account has an invalid account type.");
        }

        String reference = "DEP-" + UUID.randomUUID();

        String description = request.description() == null
                || request.description().isBlank()
                ? "Customer deposit"
                : request.description().trim();

        BankTransaction transaction = BankTransaction.createDeposit(
                reference,
                account,
                request.amount(),
                account.getCurrency(),
                description,
                user
        );

        transaction = transactionRepository.saveAndFlush(transaction);

        LedgerEntry debitEntry = LedgerEntry.create(
                transaction,
                cashLedger,
                LedgerEntrySide.DEBIT,
                request.amount(),
                account.getCurrency(),
                "Cash received - " + reference
        );

        LedgerEntry creditEntry = LedgerEntry.create(
                transaction,
                customerLedger,
                LedgerEntrySide.CREDIT,
                request.amount(),
                account.getCurrency(),
                "Customer deposit - " + reference
        );

        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);

        account.credit(request.amount());

        accountRepository.save(account);

        auditEventService.recordEvent(
                user.getId(),
                "DEPOSIT_COMPLETED",
                "TRANSACTION",
                transaction.getId().toString(),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent"),
                "{\"amount\":\"" + request.amount().toPlainString()
                        + "\",\"currency\":\"" + account.getCurrency() + "\"}"
        );

        return new TransactionResponse(
                transaction.getId(),
                transaction.getReference(),
                transaction.getTransactionType(),
                TransactionStatus.COMPLETED,
                account.getAccountNumber(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                account.getBalance(),
                transaction.getCreatedAt()
        );
    }

    @Override
    public TransactionResponse withdraw(
            String accountNumber,
            DepositRequest request,
            String idempotencyKey
    ) {
        String requestData = accountNumber.trim()
                + "|" + request.amount().stripTrailingZeros().toPlainString()
                + "|" + (request.description() == null
                ? ""
                : request.description().trim());

        String requestHash = idempotencyHashService.hash(requestData);

        return idempotencyService.execute(
                idempotencyKey,
                requestHash,
                () -> processWithdrawal(accountNumber, request)
        );
    }

    @Transactional
    public TransactionResponse processWithdrawal(
            String accountNumber,
            DepositRequest request
    ) {
        User user = currentUserService.getCurrentUser();

        Account account = accountRepository
                .findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found."
                ));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "You are not authorised to access this account."
            );
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Account is not active."
            );
        }

        if (!"USD".equals(account.getCurrency())) {
            throw new IllegalArgumentException(
                    "Unsupported account currency."
            );
        }

        BigDecimal amount = request.amount();

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient funds."
            );
        }

        LedgerAccount customerLedger = account.getLedgerAccount();

        LedgerAccount bankCashLedger = ledgerAccountRepository
                .findByAccountCode("BANK-CASH-USD")
                .orElseThrow(() -> new IllegalStateException(
                        "Bank cash ledger account not found."
                ));

        String reference = "WTH-" + UUID.randomUUID();

        String description = request.description() == null
                || request.description().isBlank()
                ? "Customer withdrawal"
                : request.description();

        BankTransaction transaction =
                BankTransaction.createWithdrawal(
                        reference,
                        account,
                        amount,
                        account.getCurrency(),
                        description,
                        user
                );

        transaction = transactionRepository.saveAndFlush(transaction);

        LedgerEntry customerDebit = LedgerEntry.create(
                transaction,
                customerLedger,
                LedgerEntrySide.DEBIT,
                amount,
                account.getCurrency(),
                description
        );

        LedgerEntry bankCredit = LedgerEntry.create(
                transaction,
                bankCashLedger,
                LedgerEntrySide.CREDIT,
                amount,
                account.getCurrency(),
                description
        );

        ledgerEntryRepository.save(customerDebit);
        ledgerEntryRepository.save(bankCredit);

        account.debit(amount);

        accountRepository.save(account);

        auditEventService.recordEvent(
                user.getId(),
                "WITHDRAWAL_COMPLETED",
                "TRANSACTION",
                transaction.getId().toString(),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent"),
                "{\"amount\":\"" + amount.toPlainString()
                        + "\",\"currency\":\"" + account.getCurrency() + "\"}"
        );

        return new TransactionResponse(
                transaction.getId(),
                transaction.getReference(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                account.getAccountNumber(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                account.getBalance(),
                transaction.getCreatedAt()
        );
    }

    @Override
    public TransactionResponse transfer(
            TransferRequest request,
            String idempotencyKey
    ) {
        String requestData =
                request.sourceAccountNumber().trim()
                        + "|" + request.destinationAccountNumber().trim()
                        + "|" + request.amount().stripTrailingZeros().toPlainString()
                        + "|" + (request.description() == null
                        ? ""
                        : request.description().trim());

        String requestHash = idempotencyHashService.hash(requestData);

        return idempotencyService.execute(
                idempotencyKey,
                requestHash,
                () -> processTransfer(request)
        );
    }

    @Transactional
    public TransactionResponse processTransfer(TransferRequest request) {

        User user = currentUserService.getCurrentUser();

        String destinationNumber = request.destinationAccountNumber();

        // Find both accounts first to determine their IDs.
        Account sourceReference = accountRepository
                .findByAccountNumber(
                        request.sourceAccountNumber()
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Source account not found."
                ));

        Account destinationReference = accountRepository
                .findByAccountNumber(destinationNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Destination account not found."
                ));

        Long sourceId = sourceReference.getId();
        Long destinationId = destinationReference.getId();

        if (sourceId.equals(destinationId)) {
            throw new IllegalArgumentException(
                    "You cannot transfer money to the same account."
            );
        }

        // Lock accounts in ascending ID order to reduce deadlocks.
        Account firstLocked;
        Account secondLocked;

        if (sourceId < destinationId) {
            firstLocked = accountRepository.findByIdForUpdate(sourceId)
                    .orElseThrow();
            secondLocked = accountRepository.findByIdForUpdate(destinationId)
                    .orElseThrow();
        } else {
            firstLocked = accountRepository.findByIdForUpdate(destinationId)
                    .orElseThrow();
            secondLocked = accountRepository.findByIdForUpdate(sourceId)
                    .orElseThrow();
        }

        Account sourceAccount = sourceId.equals(firstLocked.getId())
                ? firstLocked : secondLocked;

        Account destinationAccount =
                destinationId.equals(firstLocked.getId())
                        ? firstLocked : secondLocked;

        if (!sourceAccount.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "You are not authorised to access the source account."
            );
        }

        if (sourceAccount.getStatus() != AccountStatus.ACTIVE
                || destinationAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Both accounts must be active."
            );
        }

        if (!sourceAccount.getCurrency().equals(
                destinationAccount.getCurrency())) {
            throw new IllegalArgumentException(
                    "Account currencies must match."
            );
        }

        if (!"USD".equals(sourceAccount.getCurrency())) {
            throw new IllegalArgumentException(
                    "Unsupported account currency."
            );
        }

        BigDecimal amount = request.amount();

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds.");
        }

        String description = request.description() == null
                || request.description().isBlank()
                ? "Domestic account transfer"
                : request.description();

        String reference = "TRF-" + UUID.randomUUID();

        BankTransaction transaction = BankTransaction.createTransfer(
                reference,
                sourceAccount,
                destinationAccount,
                amount,
                sourceAccount.getCurrency(),
                description,
                user
        );

        transaction = transactionRepository.saveAndFlush(transaction);

        LedgerEntry debitEntry = LedgerEntry.create(
                transaction,
                sourceAccount.getLedgerAccount(),
                LedgerEntrySide.DEBIT,
                amount,
                sourceAccount.getCurrency(),
                description
        );

        LedgerEntry creditEntry = LedgerEntry.create(
                transaction,
                destinationAccount.getLedgerAccount(),
                LedgerEntrySide.CREDIT,
                amount,
                destinationAccount.getCurrency(),
                description
        );

        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);

        sourceAccount.debit(amount);
        destinationAccount.credit(amount);

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        auditEventService.recordEvent(
                user.getId(),
                "TRANSFER_COMPLETED",
                "TRANSACTION",
                transaction.getId().toString(),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent"),
                "{\"amount\":\"" + amount.toPlainString()
                        + "\",\"currency\":\"" + sourceAccount.getCurrency() + "\"}"
        );

        return new TransactionResponse(
                transaction.getId(),
                transaction.getReference(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                sourceAccount.getAccountNumber(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                sourceAccount.getBalance(),
                transaction.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionHistoryResponse> getMyTransactions(
            Pageable pageable
    ) {
        User user = currentUserService.getCurrentUser();

        return transactionRepository
                .findAllBySourceAccount_User_IdOrDestinationAccount_User_Id(
                        user.getId(),
                        user.getId(),
                        pageable
                )
                .map(transaction -> new TransactionHistoryResponse(
                        transaction.getId(),
                        transaction.getReference(),
                        transaction.getTransactionType(),
                        transaction.getStatus(),

                        transaction.getSourceAccount() != null
                                ? transaction.getSourceAccount().getAccountNumber()
                                : null,

                        transaction.getDestinationAccount() != null
                                ? transaction.getDestinationAccount().getAccountNumber()
                                : null,

                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getDescription(),
                        transaction.getCreatedAt()
                ));
    }
}
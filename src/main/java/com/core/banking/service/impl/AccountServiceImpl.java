package com.core.banking.service.impl;

import com.core.banking.dto.request.CreateAccountRequest;
import com.core.banking.dto.response.AccountResponse;
import com.core.banking.entity.Account;
import com.core.banking.entity.LedgerAccount;
import com.core.banking.entity.User;
import com.core.banking.enums.*;
import com.core.banking.repository.AccountRepository;
import com.core.banking.repository.LedgerAccountRepository;
import com.core.banking.security.CurrentUserService;
import com.core.banking.service.AccountService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private static final String CURRENCY = "USD";
    private static final int MAX_ATTEMPTS = 10;

    private final AccountRepository accountRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final CurrentUserService currentUserService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountServiceImpl(
            AccountRepository accountRepository,
            LedgerAccountRepository ledgerAccountRepository,
            CurrentUserService currentUserService) {

        this.accountRepository = accountRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {

        User user = currentUserService.getCurrentUser();

        String accountNumber = generateUniqueAccountNumber();
        String accountCode = "CUSTOMER-" + accountNumber;

        LedgerAccount ledgerAccount = LedgerAccount.create(
                accountCode,
                request.accountName(),
                LedgerAccountType.LIABILITY,
                CURRENCY,
                LedgerAccountStatus.ACTIVE
        );

        ledgerAccount = ledgerAccountRepository.saveAndFlush(ledgerAccount);

        Account account = Account.create(
                user,
                ledgerAccount,
                accountNumber,
                request.accountName().trim(),
                request.accountType(),
                CURRENCY
        );

        account = accountRepository.saveAndFlush(account);

        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts() {

        User user = currentUserService.getCurrentUser();

        return accountRepository.findAllByUser_Id(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private String generateUniqueAccountNumber() {

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {

            long number = secureRandom.nextLong(
                    100_000_000_000L,
                    1_000_000_000_000L
            );

            String accountNumber = String.valueOf(number);

            String accountCode = "CUSTOMER-" + accountNumber;

            if (!accountRepository.existsByAccountNumber(accountNumber)
                    && ledgerAccountRepository.findByAccountCode(accountCode).isEmpty()) {

                return accountNumber;
            }
        }

        throw new IllegalStateException(
                "Unable to generate a unique bank account number."
        );
    }

    private AccountResponse mapToResponse(Account account) {

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountName(),
                account.getAccountType(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }
}

package com.core.banking.dto.response;

import com.core.banking.enums.AccountStatus;
import com.core.banking.enums.AccountType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AccountResponse(

        Long id,
        String accountNumber,
        String accountName,
        AccountType accountType,
        String currency,
        BigDecimal balance,
        AccountStatus status,
        OffsetDateTime createdAt

) {
}
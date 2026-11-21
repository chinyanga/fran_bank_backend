package com.core.banking.dto.response;

import com.core.banking.enums.TransactionStatus;
import com.core.banking.enums.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionHistoryResponse(

        Long id,

        String reference,

        TransactionType type,

        TransactionStatus status,

        String sourceAccountNumber,

        String destinationAccountNumber,

        BigDecimal amount,

        String currency,

        String description,

        OffsetDateTime createdAt

) {}
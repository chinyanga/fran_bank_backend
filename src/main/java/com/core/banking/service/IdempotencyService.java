
package com.core.banking.service;

import com.core.banking.dto.response.TransactionResponse;

public interface IdempotencyService {

    TransactionResponse execute(
            String idempotencyKey,
            String requestHash,
            TransactionOperation operation
    );

    @FunctionalInterface
    interface TransactionOperation {
        TransactionResponse execute();
    }
}

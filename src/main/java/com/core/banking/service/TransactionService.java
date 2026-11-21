package com.core.banking.service;

import com.core.banking.dto.request.DepositRequest;
import com.core.banking.dto.request.TransferRequest;
import com.core.banking.dto.response.TransactionHistoryResponse;
import com.core.banking.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {

    TransactionResponse deposit(
            String accountNumber,
            DepositRequest request,
            String idempotencyKey
    );

    TransactionResponse withdraw(
            String accountNumber,
            DepositRequest request,
            String idempotencyKey
    );

    TransactionResponse transfer(TransferRequest request,String idempotencyKey);

    Page<TransactionHistoryResponse> getMyTransactions(Pageable pageable);
}
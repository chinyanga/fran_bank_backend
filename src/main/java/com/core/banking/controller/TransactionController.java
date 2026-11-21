package com.core.banking.controller;

import com.core.banking.dto.request.DepositRequest;
import com.core.banking.dto.request.TransferRequest;
import com.core.banking.dto.response.APIResponse;
import com.core.banking.dto.response.TransactionResponse;
import com.core.banking.service.TransactionService;

import com.core.banking.dto.response.TransactionHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/accounts/{accountNumber}/deposits")
    public ResponseEntity<APIResponse<TransactionResponse>> deposit(
            @PathVariable String accountNumber,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody DepositRequest request) {

        TransactionResponse response =
                transactionService.deposit(accountNumber, request, idempotencyKey);

        return ResponseEntity.ok(
                APIResponse.success(
                        200,
                        "Deposit completed successfully",
                        response
                )
        );
    }

    @PostMapping("/accounts/{accountNumber}/withdrawals")
    public ResponseEntity<APIResponse<TransactionResponse>> withdraw(
            @PathVariable String accountNumber,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody DepositRequest request
    ) {
        TransactionResponse response =
                transactionService.withdraw(accountNumber, request, idempotencyKey);

        return ResponseEntity.ok(
                APIResponse.success(
                        200,
                        "Withdrawal completed successfully",
                        response
                )
        );
    }

    @PostMapping("/transfers")
    public ResponseEntity<APIResponse<TransactionResponse>> transfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request
    ) {
        TransactionResponse response =
                transactionService.transfer(request, idempotencyKey);

        return ResponseEntity.ok(
                APIResponse.success(
                        200,
                        "Transfer completed successfully",
                        response
                )
        );
    }

    @GetMapping("/transactions")
    public ResponseEntity<APIResponse<Page<TransactionHistoryResponse>>>
    getMyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Invalid pagination parameters."
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<TransactionHistoryResponse> transactions =
                transactionService.getMyTransactions(pageable);

        return ResponseEntity.ok(
                APIResponse.success(
                        200,
                        "Transaction history retrieved successfully",
                        transactions
                )
        );
    }
}

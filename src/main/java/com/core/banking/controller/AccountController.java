package com.core.banking.controller;

import com.core.banking.dto.request.CreateAccountRequest;
import com.core.banking.dto.response.AccountResponse;
import com.core.banking.dto.response.APIResponse;
import com.core.banking.service.AccountService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<APIResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        AccountResponse response = accountService.createAccount(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success(
                        201,
                        "Bank account created successfully",
                        response
                ));
    }

    @GetMapping
    public ResponseEntity<APIResponse<List<AccountResponse>>> getMyAccounts() {

        List<AccountResponse> accounts = accountService.getMyAccounts();

        return ResponseEntity.ok(
                APIResponse.success(
                        200,
                        "Customer accounts retrieved successfully",
                        accounts
                )
        );
    }
}

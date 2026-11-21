package com.core.banking.service;

import com.core.banking.dto.request.CreateAccountRequest;
import com.core.banking.dto.response.AccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest request);

    List<AccountResponse> getMyAccounts();
}

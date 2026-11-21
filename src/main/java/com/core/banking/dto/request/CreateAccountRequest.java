package com.core.banking.dto.request;

import com.core.banking.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(

        @NotBlank(message = "Account name is required")
        @Size(max = 100, message = "Account name cannot exceed 100 characters")
        String accountName,

        @NotNull(message = "Account type is required")
        AccountType accountType

) {
}

package com.core.banking.dto.response;

public record AuthTokenResult(
        LoginResponse loginResponse,
        String refreshToken
) {
}
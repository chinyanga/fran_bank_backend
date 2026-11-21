package com.core.banking.service;

import com.core.banking.dto.response.AuthTokenResult;

public interface RefreshTokenService {

    AuthTokenResult refresh(String refreshToken);

    void revoke(String refreshToken);

    void revokeAllUserTokens(Long userId);
}
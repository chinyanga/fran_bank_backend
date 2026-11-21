package com.core.banking.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshCookieService {

    private final String cookieName;
    private final boolean secure;
    private final String sameSite;
    private final String path;
    private final long refreshTokenTtl;

    public RefreshCookieService(
            @Value("${spring.security.refresh-cookie.name}") String cookieName,
            @Value("${spring.security.refresh-cookie.secure}") boolean secure,
            @Value("${spring.security.refresh-cookie.same-site}") String sameSite,
            @Value("${spring.security.refresh-cookie.path}") String path,
            @Value("${spring.security.jwt.refresh-token-ttl-seconds}") long refreshTokenTtl
    ) {
        this.cookieName = cookieName;
        this.secure = secure;
        this.sameSite = sameSite;
        this.path = path;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public ResponseCookie createRefreshCookie(String refreshToken) {
        return ResponseCookie.from(cookieName, refreshToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(Duration.ofSeconds(refreshTokenTtl))
                .build();
    }

    public ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(Duration.ZERO)
                .build();
    }

    public String getCookieName() {
        return cookieName;
    }
}
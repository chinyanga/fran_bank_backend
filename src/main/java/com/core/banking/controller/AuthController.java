
package com.core.banking.controller;

import com.core.banking.dto.request.LoginRequest;
import com.core.banking.dto.request.LogoutRequest;
import com.core.banking.dto.request.RefreshTokenRequest;
import com.core.banking.dto.request.RegisterRequest;
import com.core.banking.dto.response.APIResponse;
import com.core.banking.dto.response.AuthTokenResult;
import com.core.banking.dto.response.LoginResponse;
import com.core.banking.dto.response.UserResponse;
import com.core.banking.security.RefreshCookieService;
import com.core.banking.service.AuthService;
import com.core.banking.service.RefreshTokenService;
import com.core.banking.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshCookieService refreshCookieService;

    public AuthController(
            UserService userService,
            AuthService authService,
            RefreshTokenService refreshTokenService, RefreshCookieService refreshCookieService
    ) {
        this.userService = userService;
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.refreshCookieService = refreshCookieService;
    }

    @PostMapping("/register")
    public ResponseEntity<APIResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse user = userService.register(request);

        APIResponse<UserResponse> response = APIResponse.success(
                HttpStatus.CREATED.value(),
                "Customer registered successfully",
                user
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthTokenResult result = authService.login(request);

        ResponseCookie refreshCookie =
                refreshCookieService.createRefreshCookie(
                        result.refreshToken()
                );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(
                        APIResponse.success(
                                200,
                                "Login successful",
                                result.loginResponse()
                        )
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<APIResponse<LoginResponse>> refresh(
            @CookieValue(
                    name = "franbank_refresh",
                    required = false
            ) String refreshToken
    ) {
        AuthTokenResult result = refreshTokenService.refresh(refreshToken);

        ResponseCookie refreshCookie =
                refreshCookieService.createRefreshCookie(
                        result.refreshToken()
                );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(
                        APIResponse.success(
                                200,
                                "Token refreshed successfully",
                                result.loginResponse()
                        )
                );
    }


    @PostMapping("/logout")
    public ResponseEntity<APIResponse<Void>> logout(
            @CookieValue(
                    name = "franbank_refresh",
                    required = false
            ) String refreshToken
    ) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revoke(refreshToken);
        }

        ResponseCookie clearCookie =
                refreshCookieService.clearRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(
                        APIResponse.success(
                                200,
                                "Logout successful",
                                null
                        )
                );
    }

}

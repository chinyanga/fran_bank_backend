
package com.core.banking.service.impl;

import com.core.banking.dto.request.LoginRequest;
import com.core.banking.dto.request.RegisterRequest;
import com.core.banking.dto.response.AuthTokenResult;
import com.core.banking.dto.response.LoginResponse;
import com.core.banking.dto.response.UserResponse;
import com.core.banking.entity.RefreshToken;
import com.core.banking.entity.User;
import com.core.banking.repository.RefreshTokenRepository;
import com.core.banking.repository.UserRepository;
import com.core.banking.security.JwtService;
import com.core.banking.security.RefreshTokenGenerator;
import com.core.banking.service.AuthService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class AuthenticationServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final long refreshTokenTtl;

    public AuthenticationServiceImpl(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenGenerator refreshTokenGenerator,
            @Value("${spring.security.jwt.refresh-token-ttl-seconds}")
            long refreshTokenTtl
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        throw new UnsupportedOperationException(
                "Registration is handled by UserService."
        );
    }

    @Override
    @Transactional
    public AuthTokenResult login(LoginRequest request) {
        {

            String normalizedEmail =
                    request.email().trim().toLowerCase();

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    normalizedEmail,
                                    request.password()
                            )
                    );

            UserDetails userDetails =
                    (UserDetails) authentication.getPrincipal();

            String accessToken =
                    jwtService.generateAccessToken(userDetails);

            User user = userRepository
                    .findByEmailNormalized(normalizedEmail)
                    .orElseThrow(() -> new IllegalStateException(
                            "Authenticated user could not be found."
                    ));

            String rawRefreshToken =
                    refreshTokenGenerator.generateToken();

            String hashedRefreshToken =
                    refreshTokenGenerator.hashToken(rawRefreshToken);

            RefreshToken refreshToken = RefreshToken.create(
                    user,
                    hashedRefreshToken,
                    OffsetDateTime.now().plusSeconds(refreshTokenTtl)
            );

            refreshTokenRepository.save(refreshToken);

            UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getStatus().name(),
                    user.isEmailVerified(),
                    user.getCreatedAt()
            );

            LoginResponse loginResponse = new LoginResponse(
                    accessToken,
                    "Bearer",
                    900,
                    userResponse
            );

            return new AuthTokenResult(
                    loginResponse,
                    rawRefreshToken
            );
        }
    }
}

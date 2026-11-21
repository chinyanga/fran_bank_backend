
package com.core.banking.service.impl;

import com.core.banking.dto.response.AuthTokenResult;
import com.core.banking.dto.response.LoginResponse;
import com.core.banking.dto.response.UserResponse;
import com.core.banking.entity.RefreshToken;
import com.core.banking.entity.User;
import com.core.banking.exception.InvalidRefreshTokenException;
import com.core.banking.repository.RefreshTokenRepository;
import com.core.banking.security.CustomUserDetailsService;
import com.core.banking.security.JwtService;
import com.core.banking.security.RefreshTokenGenerator;
import com.core.banking.service.RefreshTokenService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final long refreshTokenTtl;
    private final long accessTokenTtl;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenGenerator refreshTokenGenerator,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService,
            @Value("${spring.security.jwt.refresh-token-ttl-seconds}")
            long refreshTokenTtl,
            @Value("${spring.security.jwt.access-token-ttl-seconds}")
            long accessTokenTtl
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.refreshTokenTtl = refreshTokenTtl;
        this.accessTokenTtl = accessTokenTtl;
    }

    @Override
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthTokenResult refresh(String rawRefreshToken) {

        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        String tokenHash =
                refreshTokenGenerator.hashToken(rawRefreshToken);

        RefreshToken oldToken = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        User user = oldToken.getUser();

        if (oldToken.isRevoked()) {
            revokeAllUserTokens(user.getId());
            throw new InvalidRefreshTokenException();
        }

        if (oldToken.isExpired()) {
            oldToken.revoke();
            throw new InvalidRefreshTokenException();
        }

        String newRawRefreshToken =
                refreshTokenGenerator.generateToken();

        String newTokenHash =
                refreshTokenGenerator.hashToken(newRawRefreshToken);

        RefreshToken newToken = RefreshToken.create(
                user,
                newTokenHash,
                OffsetDateTime.now().plusSeconds(refreshTokenTtl)
        );

        oldToken.replaceWith(newToken);

        refreshTokenRepository.save(newToken);
        refreshTokenRepository.save(oldToken);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getEmailNormalized());

        String newAccessToken =
                jwtService.generateAccessToken(userDetails);

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
                newAccessToken,
                "Bearer",
                accessTokenTtl,
                userResponse
        );

        return new AuthTokenResult(
                loginResponse,
                newRawRefreshToken
        );
    }

    @Override
    @Transactional
    public void revoke(String rawRefreshToken) {

        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        String tokenHash =
                refreshTokenGenerator.hashToken(rawRefreshToken);

        RefreshToken token = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        token.revoke();
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(Long userId) {

        List<RefreshToken> activeTokens =
                refreshTokenRepository.findAllByUser_IdAndRevokedFalse(userId);

        activeTokens.forEach(RefreshToken::revoke);
    }
}

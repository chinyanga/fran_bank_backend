package com.core.banking.dto.response;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String status,
        boolean emailVerified,
        OffsetDateTime createdAt
) {
}

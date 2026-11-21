package com.core.banking.dto.response;

import java.time.OffsetDateTime;

public record APIResponse<T>(
        int status,
        String message,
        T data,
        OffsetDateTime timestamp
) {

    public static <T> APIResponse<T> success(
            int status,
            String message,
            T data
    ) {
        return new APIResponse<>(
                status,
                message,
                data,
                OffsetDateTime.now()
        );
    }

    public static <T> APIResponse<T> error(
            int status,
            String message,
            T data
    ) {
        return new APIResponse<>(
                status,
                message,
                data,
                OffsetDateTime.now()
        );
    }
}

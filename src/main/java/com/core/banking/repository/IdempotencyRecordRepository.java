
package com.core.banking.repository;

import com.core.banking.entity.IdempotencyRecord;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IdempotencyRecordRepository
        extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByUser_IdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM IdempotencyRecord i
            WHERE i.user.id = :userId
            AND i.idempotencyKey = :idempotencyKey
            """)
    Optional<IdempotencyRecord> findByUserAndKeyForUpdate(
            @Param("userId") Long userId,
            @Param("idempotencyKey") String idempotencyKey
    );
}

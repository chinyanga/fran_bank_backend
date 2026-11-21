package com.core.banking.repository;

import com.core.banking.entity.LedgerAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LedgerAccountRepository
        extends JpaRepository<LedgerAccount, Long> {

    Optional<LedgerAccount> findByAccountCode(String accountCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT la
        FROM LedgerAccount la
        WHERE la.accountCode = :accountCode
        """)
    Optional<LedgerAccount> findByAccountCodeForUpdate(
            @Param("accountCode") String accountCode
    );
}
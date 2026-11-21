package com.core.banking.repository;

import com.core.banking.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BankTransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    Optional<BankTransaction> findByReference(String reference);

    List<BankTransaction> findAllBySourceAccount_IdOrDestinationAccount_Id(
            Long sourceAccountId,
            Long destinationAccountId
    );

    Page<BankTransaction> findAllBySourceAccount_User_IdOrDestinationAccount_User_Id(
            Long sourceUserId,
            Long destinationUserId,
            Pageable pageable
    );
}
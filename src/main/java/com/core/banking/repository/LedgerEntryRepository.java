package com.core.banking.repository;

import com.core.banking.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findAllByTransaction_Id(Long transactionId);

    List<LedgerEntry> findAllByLedgerAccount_Id(Long ledgerAccountId);
}
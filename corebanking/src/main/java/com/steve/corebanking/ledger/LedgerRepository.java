package com.steve.corebanking.ledger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {
    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerEntry l WHERE l.account.id = :accountId")
    BigDecimal sumBalanceByAccountId(Long accountId);
    Page<LedgerEntry> findByAccountId(Long accountId, Pageable pageable);
    List<LedgerEntry> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);

    Page<LedgerEntry> findByAccountNumber(String accountNumber, Pageable pageable);
}
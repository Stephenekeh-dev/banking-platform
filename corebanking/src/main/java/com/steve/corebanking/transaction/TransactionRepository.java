package com.steve.corebanking.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountNumber(String accountNumber);
    List<Transaction> findByTargetAccount(String accountNumber);
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.accountNumber = :accountNumber
          AND t.timestamp BETWEEN :start AND :end
    """)
    List<Transaction> findByAccountNumberAndTransactionDateBetween(
            @Param("accountNumber") String accountNumber,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    boolean existsByReferenceId(String referenceId);
    List<Transaction> findByAccountNumberOrTargetAccount(String accountNumber, String targetAccount);
    Page<Transaction> findByAccountNumberOrTargetAccount(
            String accountNumber, String targetAccount, Pageable pageable
    );

}

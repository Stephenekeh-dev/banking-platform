package com.steve.corebanking.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountNumber(String accountNumber);
    List<Transaction> findByTargetAccount(String accountNumber);
    boolean existsByReferenceId(String referenceId);
    List<Transaction> findByAccountNumberOrTargetAccount(String accountNumber, String targetAccount);
    Page<Transaction> findByAccountNumberOrTargetAccount(
            String accountNumber, String targetAccount, Pageable pageable
    );

}

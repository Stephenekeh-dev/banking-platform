package com.steve.corebanking.ledger;

import com.steve.corebanking.account.Account;
import com.steve.corebanking.account.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final AccountRepository accountRepository;

    public LedgerService(LedgerRepository ledgerRepository, AccountRepository accountRepository) {
        this.ledgerRepository = ledgerRepository;
        this.accountRepository = accountRepository;
    }


    // ============================
    //   CREATE SINGLE LEDGER ENTRY
    // ============================
    public LedgerEntry createEntry(LedgerEntry entry) {
        entry.setCreatedAt(LocalDateTime.now());
        return ledgerRepository.save(entry);
    }


    // ============================
    //   DOUBLE ENTRY CREATION
    // ============================
    public void createDoubleEntry(
            String debitAccount,
            String creditAccount,
            BigDecimal amount,
            String transactionId,
            String narration
    ) {
        // 1. Debit entry
        LedgerEntry debit = new LedgerEntry();
        debit.setAccountNumber(debitAccount);
        debit.setAmount(amount);
        debit.setEntryType(LedgerEntry.EntryType.DEBIT);
        debit.setTransactionId(transactionId);
        debit.setNarration("DEBIT: " + narration);

        ledgerRepository.save(debit);

        // 2. Credit entry
        LedgerEntry credit = new LedgerEntry();
        credit.setAccountNumber(creditAccount);
        credit.setAmount(amount);
        credit.setEntryType(LedgerEntry.EntryType.CREDIT);
        credit.setTransactionId(transactionId);
        credit.setNarration("CREDIT: " + narration);

        ledgerRepository.save(credit);
    }


    // ============================
    //   GET LEDGER FOR ACCOUNT
    // ============================
    public List<LedgerEntry> getLedgerForAccount(String accountNumber) {
        return ledgerRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber);
    }


    // ============================
    //   PAGINATED LEDGER
    // ============================
    public Page<LedgerEntry> getLedgerForAccount(String accountNumber, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ledgerRepository.findByAccountNumber(accountNumber, pageable);
    }


    // ============================
    //   BALANCE RECONCILIATION ENGINE
    // ============================
    public BigDecimal calculateLedgerBalance(String accountNumber) {

        List<LedgerEntry> entries =
                ledgerRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber);

        BigDecimal balance = BigDecimal.ZERO;

        for (LedgerEntry e : entries) {
            if (e.getEntryType() == LedgerEntry.EntryType.CREDIT) {
                balance = balance.add(e.getAmount());
            } else {
                balance = balance.subtract(e.getAmount());
            }
        }

        return balance;
    }


    // ============================
    //   RECONCILE LEDGER WITH ACCOUNT TABLE BALANCE
    // ============================
    public boolean reconcileAccountBalance(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        BigDecimal ledgerBalance = calculateLedgerBalance(accountNumber);

        return ledgerBalance.compareTo(account.getBalance()) == 0;
    }
}

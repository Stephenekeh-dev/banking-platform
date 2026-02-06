package com.steve.corebanking.transaction;

import com.steve.corebanking.account.AccountService;
import com.steve.corebanking.exception.NotFoundException;
import com.steve.corebanking.ledger.LedgerEntry;
import com.steve.corebanking.ledger.LedgerService;
import com.steve.corebanking.transaction.dto.TransactionDto;
import com.steve.corebanking.transaction.dto.TransactionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.steve.corebanking.transaction.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final LedgerService ledgerService;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountService accountService,
                              LedgerService ledgerService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.ledgerService = ledgerService;
    }

    private TransactionResponseDto toDto(Transaction tx) {
        TransactionResponseDto dto = new TransactionResponseDto();
        dto.setReferenceId(tx.getReferenceId());
        dto.setAccountNumber(tx.getAccountNumber());
        dto.setTargetAccount(tx.getTargetAccount());
        dto.setAmount(tx.getAmount());
        dto.setType(tx.getType());
        dto.setNarration(tx.getNarration());
        dto.setTimestamp(tx.getCreatedAt().toString());
        return dto;
    }


    @Transactional
    public TransactionResponseDto createTransaction(TransactionDto dto) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }

        // ⬇️ ADD THIS HERE (before the switch)
        if (dto.getType() == TransactionType.TRANSFER &&
                dto.getAccountNumber().equals(dto.getTargetAccount())) {
            throw new IllegalArgumentException("Source and target accounts cannot be the same");
        }


        Transaction tx = new Transaction();
        tx.setReferenceId(UUID.randomUUID().toString());
        tx.setAccountNumber(dto.getAccountNumber());
        tx.setAmount(dto.getAmount());
        tx.setType(dto.getType());
        tx.setNarration(dto.getNarration());
        tx.setTargetAccount(dto.getTargetAccount());

        // ✅ ADD THIS CHECK HERE (BEFORE ANY FINANCIAL OPERATION)
        if (transactionRepository.existsByReferenceId(tx.getReferenceId())) {
            throw new IllegalStateException("Duplicate transaction attempt");
        }

        switch (dto.getType()) {
            case DEPOSIT:
                accountService.creditAccount(dto.getAccountNumber(), dto.getAmount());
                ledgerService.createDoubleEntry(
                        "BANK_CASH_ACCOUNT",
                        dto.getAccountNumber(),
                        dto.getAmount(),
                        tx.getReferenceId(),
                        dto.getNarration()
                );
                break;

            case WITHDRAWAL:
                accountService.debitAccount(dto.getAccountNumber(), dto.getAmount());
                ledgerService.createDoubleEntry(
                        dto.getAccountNumber(),
                        "BANK_CASH_ACCOUNT",
                        dto.getAmount(),
                        tx.getReferenceId(),
                        dto.getNarration()
                );
                break;

            case TRANSFER:
                accountService.debitAccount(dto.getAccountNumber(), dto.getAmount());
                accountService.creditAccount(dto.getTargetAccount(), dto.getAmount());
                ledgerService.createDoubleEntry(
                        dto.getAccountNumber(),
                        dto.getTargetAccount(),
                        dto.getAmount(),
                        tx.getReferenceId(),
                        dto.getNarration()
                );
                break;
            default:
                throw new IllegalArgumentException("Unsupported transaction type");
        }

        return toDto(transactionRepository.save(tx));
    }

    private LedgerEntry createLedgerEntry(String accNum, String txRef, java.math.BigDecimal amount, LedgerEntry.EntryType type, String narration) {
        LedgerEntry e = new LedgerEntry();
        e.setAccountNumber(accNum);
        e.setTransactionId(txRef);
        e.setAmount(amount);
        e.setEntryType(type);
        e.setNarration(narration == null ? "" : narration);
        return e;
    }

    public List<Transaction> getAllTransactions() {

        return transactionRepository.findAll();
    }

    public Page<Transaction> getTransactionsByAccount(String accountNumber, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return transactionRepository.findByAccountNumberOrTargetAccount(
                accountNumber,
                accountNumber,
                pageable
        );
    }

    public Page<Transaction> getTransactionsForAccount(
            String accountNumber, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        return transactionRepository.findByAccountNumberOrTargetAccount(
                accountNumber,
                accountNumber,   // use same field for both src & dest
                pageable
        );
    }


}

package com.steve.corebanking.transaction;

import com.steve.corebanking.transaction.dto.TransactionDto;
import com.steve.corebanking.transaction.dto.TransactionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PreAuthorize("hasRole('TELLER')")
    @PostMapping("/create")
    public ResponseEntity<?> createTransaction(@RequestBody TransactionDto dto) {
        return ResponseEntity.ok(transactionService.createTransaction(dto));
    }
    
    @PreAuthorize("hasRole('TELLER') or hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<Transaction>> all() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @PreAuthorize("hasRole('TELLER') or hasRole('AUDITOR') or hasRole('ADMIN')")
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<Page<Transaction>> byAccount(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                transactionService.getTransactionsByAccount(accountNumber, page, size)
        );
    }
    @PreAuthorize("hasRole('TELLER') or hasRole('AUDITOR') or hasRole('ADMIN')")
    @GetMapping("/history/{accountNumber}")
    public Page<TransactionResponseDto> getHistory(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Transaction> txPage = transactionService.getTransactionsForAccount(accountNumber, page, size);

        return txPage.map(this::convertToResponse);
    }

    private TransactionResponseDto convertToResponse(Transaction tx) {
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


}

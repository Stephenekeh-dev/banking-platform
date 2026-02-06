package com.steve.corebanking.ledger;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    // Fetch all ledger entries for a specific account (AUDITOR only)
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")
    @GetMapping("/all/{accountNumber}")
    public ResponseEntity<?> getLedgerEntries(@PathVariable String accountNumber) {
        return ResponseEntity.ok(ledgerService.getLedgerForAccount(accountNumber));
    }

    // Paginated ledger entries for an account (AUDITOR only)
    @PreAuthorize("hasRole('AUDITOR')")
    @GetMapping("/page/{accountNumber}")
    public ResponseEntity<Page<LedgerEntry>> getLedger(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ledgerService.getLedgerForAccount(accountNumber, page, size));
    }

    // Reconciliation endpoint
    @PreAuthorize("hasRole('AUDITOR')")
    @GetMapping("/reconcile/{accountNumber}")
    public ResponseEntity<String> reconcileAccount(@PathVariable String accountNumber) {
        boolean isBalanced = ledgerService.reconcileAccountBalance(accountNumber);
        String message = isBalanced
                ? "Ledger balance matches account balance."
                : "Discrepancy detected! Ledger balance does not match account balance.";
        return ResponseEntity.ok(message);
    }
}

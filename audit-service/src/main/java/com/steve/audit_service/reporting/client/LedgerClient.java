package com.steve.audit_service.reporting.client;


import com.steve.audit_service.reporting.dto.LedgerEntryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "corebanking",
        url = "${corebanking.service.url}"
)
public interface LedgerClient {

    // Matches: GET /api/ledger/all/{accountNumber}
    @GetMapping("/api/ledger/all/{accountNumber}")
    List<LedgerEntryDto> getLedgerEntries(
            @PathVariable("accountNumber") String accountNumber
    );

    // Matches: GET /api/ledger/page/{accountNumber}
    @GetMapping("/api/ledger/page/{accountNumber}")
    List<LedgerEntryDto> getLedgerForAccount(
            @PathVariable("accountNumber") String accountNumber,  // ← Long → String
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    // Matches: GET /api/ledger/reconcile/{accountNumber}
    @GetMapping("/api/ledger/reconcile/{accountNumber}")
    String reconcileAccount(
            @PathVariable("accountNumber") String accountNumber   // ← new method
    );
}
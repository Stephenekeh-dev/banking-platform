package com.steve.audit_service.reporting.client;


import com.steve.audit_service.reporting.dto.LedgerEntryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "corebanking", url = "http://localhost:8080/api/ledger")
public interface LedgerClient {

    @GetMapping("/api/ledger/all/{accountNumber}")
    List<LedgerEntryDto> getLedgerEntries(
            @PathVariable("accountNumber") String accountNumber
    );

    @GetMapping("/ledger/{accountId}")
    List<LedgerEntryDto> getLedgerForAccount(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );
}
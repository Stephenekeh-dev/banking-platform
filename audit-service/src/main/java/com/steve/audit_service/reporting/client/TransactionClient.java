package com.steve.audit_service.reporting.client;

import com.steve.audit_service.reporting.dto.TransactionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
@FeignClient(
        name = "corebanking",
        contextId = "transactionClient",
        url = "${corebanking.service.url}"
)
public interface TransactionClient {

    @GetMapping("/api/transactions/all")
    List<TransactionDto> getAllTransactions();

    @GetMapping("/api/transactions/account/{accountNumber}")
    List<TransactionDto> getTransactionsByAccount(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @GetMapping("/api/transactions/account/{accountNumber}/range")
    List<TransactionDto> getTransactionsByAccountAndDateRange(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate  // ← fix here
    );
}
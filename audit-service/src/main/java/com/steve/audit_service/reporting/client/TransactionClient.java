package com.steve.audit_service.reporting.client;

import com.steve.audit_service.reporting.dto.TransactionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "corebanking", contextId = "transactionClient", url = "http://localhost:8080/api/transactions")
public interface TransactionClient {

    @GetMapping("/all")
    List<TransactionDto> getAllTransactions();

    @GetMapping("/account/{accountNumber}")
    List<TransactionDto> getTransactionsByAccount(@PathVariable String accountNumber);
}

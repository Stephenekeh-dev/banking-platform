package com.steve.audit_service.audit;

import com.steve.audit_service.reporting.client.TransactionClient;
import com.steve.audit_service.reporting.dto.TransactionDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit/test")
public class AuditTransactionTestController {

    private final TransactionClient transactionClient;

    public AuditTransactionTestController(TransactionClient transactionClient) {
        this.transactionClient = transactionClient;
    }

    @GetMapping("/transactions")
    public List<TransactionDto> testAllTransactions() {
        return transactionClient.getAllTransactions();
    }

    @GetMapping("/transactions/{accountNumber}")
    public List<TransactionDto> testByAccount(
            @PathVariable String accountNumber) {
        return transactionClient.getTransactionsByAccount(accountNumber);
    }
}

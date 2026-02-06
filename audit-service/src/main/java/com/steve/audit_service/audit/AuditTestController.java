package com.steve.audit_service.audit;

import com.steve.audit_service.reporting.client.AccountClient;
import com.steve.audit_service.reporting.client.LedgerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit/test")
public class AuditTestController {

    private final AccountClient accountFeignClient;
    private  final LedgerClient ledgerClient;
    public AuditTestController(AccountClient accountFeignClient, LedgerClient ledgerClient) {
        this.accountFeignClient = accountFeignClient;
        this.ledgerClient = ledgerClient;
    }

    @GetMapping("/accounts")
    public Object testFeign() {
        return accountFeignClient.getAllAccounts();
    }
    @GetMapping("/ledger/all/{accountNumber}")
    public Object testLedger(@PathVariable String accountNumber) {
        return ledgerClient.getLedgerEntries(accountNumber);
    }
}

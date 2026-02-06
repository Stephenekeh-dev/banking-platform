package com.steve.audit_service.reporting.client;


import com.steve.audit_service.reporting.dto.AccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "corebanking",
        url = "http://localhost:8080"
)
public interface AccountClient {

    @GetMapping("/api/accounts/all")
    Object getAllAccounts();

    @GetMapping("/api/accounts/{accountNumber}")
   AccountDto getAccount(
           @PathVariable("accountNumber") String accountNumber
    );

    @GetMapping("/accounts")
    List<AccountDto> fetchAccounts();
}

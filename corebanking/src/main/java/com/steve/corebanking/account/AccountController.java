package com.steve.corebanking.account;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody AccountCreateRequest request) {
        return ResponseEntity.ok(
                accountService.createAccount(request.getCustomerId(), request.getAccountType())
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    //@PreAuthorize("permitAll()")
    @GetMapping("/all")
    public ResponseEntity<?> all() {

        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {

        return ResponseEntity.ok(accountService.getAccount(id));
    }
}


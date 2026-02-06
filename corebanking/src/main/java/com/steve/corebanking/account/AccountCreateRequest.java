package com.steve.corebanking.account;

import lombok.Data;

@Data
public class AccountCreateRequest {
    private Long customerId;
    private AccountType accountType;
}

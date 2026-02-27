package com.steve.audit_service.reporting.dto;

import com.steve.audit_service.audit.AccountType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountDto {
    private String accountNumber;
    private String customerId;
    private BigDecimal balance;
    private AccountType accountType;
}
package com.steve.audit_service.reporting.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountDto {
    private String accountNumber;
    private String customerId;
    private BigDecimal balance;
    private String accountType;
}
package com.steve.audit_service.reporting.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private String referenceId;
    private String accountNumber;
    private String targetAccount;
    private BigDecimal amount;
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER
    private String narration;
    private LocalDateTime createdAt;
    private String createdBy; // teller username
}
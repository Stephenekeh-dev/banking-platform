package com.steve.corebanking.transaction.dto;

import com.steve.corebanking.transaction.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private String referenceId;
    private String accountNumber;
    private String targetAccount;
    private BigDecimal amount;
    private TransactionType type; // use enum instead of String
    private String narration;
    private LocalDateTime createdAt;
    private String createdBy;
}
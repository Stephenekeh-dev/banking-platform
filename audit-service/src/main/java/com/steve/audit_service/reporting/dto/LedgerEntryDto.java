package com.steve.audit_service.reporting.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LedgerEntryDto {
    private String accountNumber;
    private String transactionId;
    private BigDecimal amount;
    private EntryType entryType; // DEBIT or CREDIT
    private String narration;
    private LocalDateTime timestamp;
}
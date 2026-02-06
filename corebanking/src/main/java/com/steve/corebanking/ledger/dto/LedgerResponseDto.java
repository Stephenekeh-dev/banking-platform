package com.steve.corebanking.ledger.dto;

import com.steve.corebanking.ledger.LedgerEntry;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LedgerResponseDto {
    private String accountNumber;
    private String transactionId;
    private BigDecimal amount;
    private LedgerEntry.EntryType entryType;
    private String narration;
    private LocalDateTime createdAt;
}

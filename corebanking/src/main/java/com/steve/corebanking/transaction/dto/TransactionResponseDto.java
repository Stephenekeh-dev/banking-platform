package com.steve.corebanking.transaction.dto;


import com.steve.corebanking.transaction.TransactionType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionResponseDto {

    private String referenceId;
    private String accountNumber;
    private String targetAccount;
    private BigDecimal amount;
    private TransactionType type;
    private String narration;
    private String timestamp;

    // getters and setters
}

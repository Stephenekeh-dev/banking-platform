package com.steve.corebanking.transaction.dto;

import com.steve.corebanking.transaction.TransactionType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDto {
    private String accountNumber;        // source for withdrawal/transfer, target for deposit
    private BigDecimal amount;
    private TransactionType type;
    private String targetAccount;        // required for TRANSFER
    private String narration;
}
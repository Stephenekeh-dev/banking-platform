package com.steve.corebanking.transaction;

import com.steve.corebanking.transaction.dto.TransactionDto;
import com.steve.corebanking.transaction.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionDto toDto(Transaction tx) {
        TransactionDto dto = new TransactionDto();
        dto.setReferenceId(tx.getReferenceId());
        dto.setAccountNumber(tx.getAccountNumber());
        dto.setTargetAccount(tx.getTargetAccount());
        dto.setAmount(tx.getAmount());
        dto.setType(tx.getType()); // directly set enum
        dto.setNarration(tx.getNarration());
        dto.setCreatedAt(tx.getCreatedAt());
        dto.setCreatedBy(tx.getCreatedBy());
        return dto;
    }
}
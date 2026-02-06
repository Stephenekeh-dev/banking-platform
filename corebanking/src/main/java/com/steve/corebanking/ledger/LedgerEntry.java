package com.steve.corebanking.ledger;

import com.steve.corebanking.account.Account;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private String transactionId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private EntryType entryType;  // DEBIT or CREDIT

    private String narration;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum EntryType {
        DEBIT, CREDIT
    }
}
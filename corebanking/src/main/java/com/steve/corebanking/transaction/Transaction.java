package com.steve.corebanking.transaction;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String referenceId; // unique ref (optional UUID)
    private String accountNumber; // primary account
    private String targetAccount; // for transfer
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private String narration;
    private LocalDateTime timestamp = LocalDateTime.now();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private String createdBy;
}

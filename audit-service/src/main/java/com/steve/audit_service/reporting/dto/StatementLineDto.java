package com.steve.audit_service.reporting.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StatementLineDto {

    private LocalDateTime date;
    private String narration;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal runningBalance;
    private String reference;
}

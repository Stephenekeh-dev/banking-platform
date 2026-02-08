package com.steve.audit_service.reporting.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class AccountStatementDto {

    private String accountNumber;
    private String accountType;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<StatementLineDto> transactions;
}
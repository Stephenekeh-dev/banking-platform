package com.steve.audit_service.reporting;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;


    //  Daily Transaction Report

    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyTransactionReport(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Object> report = reportingService.generateDailyTransactionReport(date);
        return ResponseEntity.ok(report);
    }


    //  Ledger Reconciliation Report

    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @GetMapping("/reconciliation")
    public ResponseEntity<Map<String, Object>> getLedgerReconciliationReport() {
        Map<String, Object> report = reportingService.generateLedgerReconciliationReport();
        return ResponseEntity.ok(report);
    }


    // Account Statement (PDF)

    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @GetMapping("/statement/{accountNumber}")
    public ResponseEntity<?> getAccountStatement(
            @PathVariable String accountNumber,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return reportingService.generateAccountStatement(accountNumber, startDate, endDate);
    }
}

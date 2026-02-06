package com.steve.audit_service.reporting;

import com.steve.audit_service.audit.AuditActionType;
import com.steve.audit_service.audit.AuditTrailRepository;
import com.steve.audit_service.reporting.client.AccountClient;
import com.steve.audit_service.reporting.client.LedgerClient;
import com.steve.audit_service.reporting.client.TransactionClient;
import com.steve.audit_service.reporting.dto.*;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final TransactionClient transactionClient;
    private final LedgerClient ledgerClient;
    private final AccountClient accountClient;
    private final AuditTrailRepository auditTrailRepository;

    // ==================================================
    // 1️⃣ DAILY TRANSACTION REPORT
    // ==================================================
    public Map<String, Object> generateDailyTransactionReport(LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<TransactionDto> transactions = transactionClient.getAllTransactions()
                .stream()
                .filter(tx -> !tx.getCreatedAt().isBefore(start)
                        && !tx.getCreatedAt().isAfter(end))
                .toList();

        BigDecimal totalDeposits = sumByType(transactions, TransactionType.DEPOSIT);
        BigDecimal totalWithdrawals = sumByType(transactions, TransactionType.WITHDRAWAL);
        BigDecimal totalTransfers = sumByType(transactions, TransactionType.TRANSFER);

        Map<String, BigDecimal> tellerKPIs =
                transactions.stream()
                        .collect(Collectors.groupingBy(
                                TransactionDto::getCreatedBy,
                                Collectors.mapping(
                                        TransactionDto::getAmount,
                                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                                )
                        ));

        return Map.of(
                "date", date,
                "totalDeposits", totalDeposits,
                "totalWithdrawals", totalWithdrawals,
                "totalTransfers", totalTransfers,
                "tellerKPIs", tellerKPIs
        );
    }

    private BigDecimal sumByType(List<TransactionDto> txs, TransactionType type) {
        return txs.stream()
                .filter(tx -> tx.getType() != null
                        && tx.getType().equalsIgnoreCase(type.name()))
                .map(TransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    // ==================================================
    // 2️⃣ LEDGER RECONCILIATION REPORT
    // ==================================================
    public Map<String, Object> generateLedgerReconciliationReport() {

        List<AccountDto> accounts = accountClient.fetchAccounts();
        List<Map<String, Object>> mismatches = new ArrayList<>();

        for (AccountDto acc : accounts) {

            // 🔑 Fetch ledger PER ACCOUNT
            List<LedgerEntryDto> ledgerEntries =
                    ledgerClient.getLedgerEntries(acc.getAccountNumber());

            BigDecimal ledgerBalance = ledgerEntries.stream()
                    .map(l -> l.getEntryType() == EntryType.CREDIT
                            ? l.getAmount()
                            : l.getAmount().negate())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (acc.getBalance().compareTo(ledgerBalance) != 0) {
                mismatches.add(Map.of(
                        "accountNumber", acc.getAccountNumber(),
                        "accountBalance", acc.getBalance(),
                        "ledgerBalance", ledgerBalance,
                        "variance", acc.getBalance().subtract(ledgerBalance)
                ));
            }
        }

        return Map.of(
                "totalAccounts", accounts.size(),
                "totalMismatches", mismatches.size(),
                "mismatchedAccounts", mismatches
        );
    }


    // ==================================================
    // 3️⃣ SUSPICIOUS ACTIVITY REPORT
    // ==================================================
    public Map<String, Object> generateSuspiciousActivityReport() {

        var suspiciousUsers =
                auditTrailRepository.findByActionType(AuditActionType.FAILED_LOGIN)
                        .stream()
                        .collect(Collectors.groupingBy(
                                a -> a.getUsername(),
                                Collectors.counting()
                        ))
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue() >= 3)
                        .map(e -> Map.of(
                                "username", e.getKey(),
                                "failedAttempts", e.getValue()
                        ))
                        .toList();

        return Map.of(
                "usersUnderReview", suspiciousUsers,
                "totalFlaggedUsers", suspiciousUsers.size()
        );
    }

    // ==================================================
    // 4️⃣ ACCOUNT STATEMENT (PDF ONLY)
    // ==================================================
    public ResponseEntity<?> generateAccountStatement(String accountNumber) {

        // 1️⃣ Fetch the account
        AccountDto account;
        try {
            account = accountClient.getAccount(accountNumber);
        } catch (FeignException.NotFound ex) {
            return ResponseEntity.notFound().build();
        }

        // 2️⃣ Fetch transactions for this account
        List<TransactionDto> transactions;
        try {
            transactions = transactionClient.getTransactionsByAccount(accountNumber);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not fetch transactions: " + ex.getMessage());
        }

        // 3️⃣ Generate PDF
        try {
            return generatePdfStatement(accountNumber, account, transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Statement generation failed: " + e.getMessage());
        }
    }


    private ResponseEntity<?> generatePdfStatement(
            String accountNumber,
            AccountDto account,
            List<TransactionDto> transactions
    ) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);

        document.open();
        document.add(new Paragraph("ACCOUNT STATEMENT"));
        document.add(new Paragraph("Account Number: " + accountNumber));
        document.add(new Paragraph("Balance: " + account.getBalance()));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);

        Stream.of(
                "Type", "Reference", "Account",
                "Target", "Amount", "Narration", "Date"
        ).forEach(h -> table.addCell(new PdfPCell(new Phrase(h))));

        for (TransactionDto tx : transactions) {
            table.addCell(tx.getType().toString());
            table.addCell(tx.getReferenceId());
            table.addCell(tx.getAccountNumber());
            table.addCell(tx.getTargetAccount() == null ? "" : tx.getTargetAccount());
            table.addCell(tx.getAmount().toString());
            table.addCell(tx.getNarration());
            table.addCell(tx.getCreatedAt().toString());
        }

        document.add(table);
        document.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=statement_" + accountNumber + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(out.toByteArray()));
    }
}

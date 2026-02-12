package com.steve.audit_service.reportTest;

import com.steve.audit_service.audit.AuditActionType;
import com.steve.audit_service.audit.AuditTrail;
import com.steve.audit_service.audit.AuditTrailRepository;
import com.steve.audit_service.reporting.ReportingService;
import com.steve.audit_service.reporting.client.AccountClient;
import com.steve.audit_service.reporting.client.LedgerClient;
import com.steve.audit_service.reporting.client.TransactionClient;
import com.steve.audit_service.reporting.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private TransactionClient transactionClient;

    @Mock
    private LedgerClient ledgerClient;

    @Mock
    private AccountClient accountClient;

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @InjectMocks
    private ReportingService reportingService;

    @Test
    void generateDailyTransactionReport_shouldAggregateCorrectly() {
        LocalDate date = LocalDate.now();

        TransactionDto deposit = new TransactionDto();
        deposit.setType("DEPOSIT");
        deposit.setAmount(new BigDecimal("100"));
        deposit.setCreatedAt(date.atTime(10, 0));
        deposit.setCreatedBy("teller1");

        TransactionDto withdrawal = new TransactionDto();
        withdrawal.setType("WITHDRAWAL");
        withdrawal.setAmount(new BigDecimal("50"));
        withdrawal.setCreatedAt(date.atTime(12, 0));
        withdrawal.setCreatedBy("teller1");

        when(transactionClient.getAllTransactions())
                .thenReturn(List.of(deposit, withdrawal));

        Map<String, Object> report =
                reportingService.generateDailyTransactionReport(date);

        assertEquals(new BigDecimal("100"), report.get("totalDeposits"));
        assertEquals(new BigDecimal("50"), report.get("totalWithdrawals"));
        assertEquals(BigDecimal.ZERO, report.get("totalTransfers"));

        Map<String, BigDecimal> tellerKPIs =
                (Map<String, BigDecimal>) report.get("tellerKPIs");

        assertEquals(new BigDecimal("150"), tellerKPIs.get("teller1"));
    }

    @Test
    void generateLedgerReconciliationReport_shouldDetectMismatch() {
        AccountDto account = new AccountDto();
        account.setAccountNumber("ACC123");
        account.setBalance(new BigDecimal("100"));

        LedgerEntryDto credit = new LedgerEntryDto();
        credit.setAmount(new BigDecimal("80"));
        credit.setEntryType(EntryType.CREDIT);

        when(accountClient.fetchAccounts())
                .thenReturn(List.of(account));

        when(ledgerClient.getLedgerEntries("ACC123"))
                .thenReturn(List.of(credit));

        Map<String, Object> report =
                reportingService.generateLedgerReconciliationReport();

        assertEquals(1, report.get("totalAccounts"));
        assertEquals(1, report.get("totalMismatches"));

        List<?> mismatches =
                (List<?>) report.get("mismatchedAccounts");

        assertEquals(1, mismatches.size());
    }

    @Test
    void generateSuspiciousActivityReport_shouldFlagUsers() {
        AuditTrail a1 = AuditTrail.builder().username("user1").build();
        AuditTrail a2 = AuditTrail.builder().username("user1").build();
        AuditTrail a3 = AuditTrail.builder().username("user1").build();

        when(auditTrailRepository.findByActionType(AuditActionType.FAILED_LOGIN))
                .thenReturn(List.of(a1, a2, a3));

        Map<String, Object> report =
                reportingService.generateSuspiciousActivityReport();

        assertEquals(1, report.get("totalFlaggedUsers"));

        List<?> users =
                (List<?>) report.get("usersUnderReview");

        assertEquals(1, users.size());
    }

    @Test
    void generateAccountStatement_shouldReturnStatement() {
        AccountDto account = new AccountDto();
        account.setAccountNumber("ACC123");
        account.setBalance(new BigDecimal("500"));

        TransactionDto tx = new TransactionDto();
        tx.setType("DEPOSIT");
        tx.setAmount(new BigDecimal("100"));
        tx.setCreatedAt(LocalDateTime.now());
        tx.setReferenceId("TX1");

        when(accountClient.getAccount("ACC123"))
                .thenReturn(account);

        when(transactionClient.getTransactionsByAccountAndDateRange(
                eq("ACC123"), any(), any()))
                .thenReturn(List.of(tx));

        ResponseEntity<?> response =
                reportingService.generateAccountStatement(
                        "ACC123",
                        LocalDate.now().minusDays(1),
                        LocalDate.now()
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AccountStatementDto);
    }



}
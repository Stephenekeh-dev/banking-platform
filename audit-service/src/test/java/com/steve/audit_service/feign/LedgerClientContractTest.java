package com.steve.audit_service.feign;

import com.steve.audit_service.audit.AuditTrailRepository;
import com.steve.audit_service.reporting.client.LedgerClient;
import com.steve.audit_service.reporting.dto.EntryType;
import com.steve.audit_service.reporting.dto.LedgerEntryDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.loadbalancer.enabled=false",
                "transaction.service.url=http://localhost:9999"
        }
)
@ActiveProfiles("test")
class LedgerClientContractTest {

    static MockWebServer mockWebServer;

    static {
        try {
            mockWebServer = new MockWebServer();
            mockWebServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "corebanking.service.url",
                () -> "http://localhost:" + mockWebServer.getPort()
        );
    }

    @TestConfiguration
    @EnableFeignClients(clients = LedgerClient.class)
    static class TestConfig {
        @Bean
        AuditTrailRepository auditTrailRepository() {
            return Mockito.mock(AuditTrailRepository.class);
        }
    }

    @Autowired
    private LedgerClient ledgerClient;

    @AfterAll
    static void shutdown() throws IOException {
        mockWebServer.shutdown();
    }


    // ══════════════════════════════════════════════════════════════════════════
    // TEST 1 — getLedgerEntries(accountNumber)
    // ══════════════════════════════════════════════════════════════════════════
    @Test
    void shouldCallGetLedgerEntriesEndpointAndMapResponse() throws Exception {

        // ── ARRANGE ──────────────────────────────────────────────────────────
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("""
                                    [
                                      {
                                        "accountNumber": "ACC123",
                                        "transactionId": "TXN001",
                                        "entryType": "CREDIT",
                                        "amount": 500.00,
                                        "narration": "Initial deposit",
                                        "timestamp": "2026-01-01T10:00:00"
                                      },
                                      {
                                        "accountNumber": "ACC123",
                                        "transactionId": "TXN002",
                                        "entryType": "DEBIT",
                                        "amount": 100.00,
                                        "narration": "Withdrawal",
                                        "timestamp": "2026-01-02T11:00:00"
                                      }
                                    ]
                                """)
        );

// ── ACT ───────────────────────────────────────────────────────────────
        List<LedgerEntryDto> entries = ledgerClient.getLedgerEntries("ACC123");
        RecordedRequest request = mockWebServer.takeRequest(3, TimeUnit.SECONDS);

// ── ASSERT: Request ───────────────────────────────────────────────────
        assertNotNull(request, "No request received by MockWebServer");
        assertEquals("GET", request.getMethod());
        assertEquals("/api/ledger/all/ACC123", request.getPath());

// ── ASSERT: Response ──────────────────────────────────────────────────
        assertNotNull(entries, "Ledger entries should not be null");
        assertEquals(2, entries.size());

        assertEquals("ACC123", entries.get(0).getAccountNumber());
        assertEquals("TXN001", entries.get(0).getTransactionId());
        assertEquals(EntryType.CREDIT, entries.get(0).getEntryType());
        assertEquals(new BigDecimal("500.00"), entries.get(0).getAmount());
        assertEquals("Initial deposit", entries.get(0).getNarration());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0, 0), entries.get(0).getTimestamp());

        assertEquals("ACC123", entries.get(1).getAccountNumber());
        assertEquals("TXN002", entries.get(1).getTransactionId());
        assertEquals(EntryType.DEBIT, entries.get(1).getEntryType());
        assertEquals(new BigDecimal("100.00"), entries.get(1).getAmount());
        assertEquals("Withdrawal", entries.get(1).getNarration());
        assertEquals(LocalDateTime.of(2026, 1, 2, 11, 0, 0), entries.get(1).getTimestamp());
    }


    // ══════════════════════════════════════════════════════════════════════════
    // TEST 2 — getLedgerForAccount(accountNumber, page, size)
    // ══════════════════════════════════════════════════════════════════════════
    @Test
    void shouldCallGetLedgerForAccountWithPaginationAndMapResponse() throws Exception {

        // ── ARRANGE ──────────────────────────────────────────────────────────
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("""
                                    [
                                      {
                                        "accountNumber": "ACC123",
                                        "transactionId": "TXN003",
                                        "entryType": "CREDIT",
                                        "amount": 200.00,
                                        "narration": "Salary",
                                        "timestamp": "2026-01-03T09:00:00"
                                      }
                                    ]
                                """)
        );

// ── ACT ───────────────────────────────────────────────────────────────
        List<LedgerEntryDto> entries = ledgerClient.getLedgerForAccount("ACC123", 0, 10);
        RecordedRequest request = mockWebServer.takeRequest(3, TimeUnit.SECONDS);

// ── ASSERT: Request ───────────────────────────────────────────────────
        assertNotNull(request, "No request received by MockWebServer");
        assertEquals("GET", request.getMethod());
        assertEquals("/api/ledger/page/ACC123?page=0&size=10", request.getPath());

// ── ASSERT: Response ──────────────────────────────────────────────────
        assertNotNull(entries, "Ledger entries should not be null");
        assertEquals(1, entries.size());

        assertEquals("ACC123", entries.get(0).getAccountNumber());
        assertEquals("TXN003", entries.get(0).getTransactionId());
        assertEquals(EntryType.CREDIT, entries.get(0).getEntryType());
        assertEquals(new BigDecimal("200.00"), entries.get(0).getAmount());
        assertEquals("Salary", entries.get(0).getNarration());
        assertEquals(LocalDateTime.of(2026, 1, 3, 9, 0, 0), entries.get(0).getTimestamp());
    }


    // ══════════════════════════════════════════════════════════════════════════
    // TEST 3 — reconcileAccount(accountNumber)
    // ══════════════════════════════════════════════════════════════════════════
    @Test
    void shouldCallReconcileEndpointAndReturnMessage() throws Exception {

        // ── ARRANGE ──────────────────────────────────────────────────────────
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "text/plain")
                        .setBody("Ledger balance matches account balance.")
        );

// ── ACT ───────────────────────────────────────────────────────────────
        String response = ledgerClient.reconcileAccount("ACC123");
        RecordedRequest request = mockWebServer.takeRequest(3, TimeUnit.SECONDS);

// ── ASSERT: Request ───────────────────────────────────────────────────
        assertNotNull(request, "No request received by MockWebServer");
        assertEquals("GET", request.getMethod());
        assertEquals("/api/ledger/reconcile/ACC123", request.getPath());

// ── ASSERT: Response ──────────────────────────────────────────────────
        assertNotNull(response, "Response should not be null");
        assertEquals("Ledger balance matches account balance.", response);
    }
}
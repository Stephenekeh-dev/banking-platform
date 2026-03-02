package com.steve.audit_service.feign;

import com.steve.audit_service.audit.AuditTrailRepository;
import com.steve.audit_service.reporting.client.TransactionClient;
import com.steve.audit_service.reporting.dto.TransactionDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.loadbalancer.enabled=false",
                "corebanking.service.url=http://localhost:9999"
        }
)
@ActiveProfiles("test")
class TransactionClientContractTest {

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
    @EnableFeignClients(clients = TransactionClient.class)
    static class TestConfig {
        @Bean
        AuditTrailRepository auditTrailRepository() {
            return Mockito.mock(AuditTrailRepository.class);
        }
    }

    @Autowired
    private TransactionClient transactionClient;

    @AfterAll
    static void shutdown() throws IOException {
        mockWebServer.shutdown();
    }



    // TEST 1 — getAllTransactions()

    @Test
    void shouldCallGetAllTransactionsEndpointAndMapResponse() throws Exception {

        //  ARRANGE
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("""
                    [
                      {
                        "referenceId": "REF001",
                        "accountNumber": "ACC123",
                        "targetAccount": null,
                        "amount": 500.00,
                        "type": "DEPOSIT",
                        "narration": "Cash deposit",
                        "createdAt": "2026-01-01T10:00:00",
                        "createdBy": "teller01"
                      },
                      {
                        "referenceId": "REF002",
                        "accountNumber": "ACC123",
                        "targetAccount": "ACC456",
                        "amount": 200.00,
                        "type": "TRANSFER",
                        "narration": "Transfer to ACC456",
                        "createdAt": "2026-01-02T11:00:00",
                        "createdBy": "teller02"
                      }
                    ]
                """)
        );

        //  ACT
        List<TransactionDto> transactions = transactionClient.getAllTransactions();
        RecordedRequest request = mockWebServer.takeRequest(3, TimeUnit.SECONDS);

        // ASSERT: Request
        assertNotNull(request,                       "No request received by MockWebServer");
        Assertions.assertEquals("GET", request.getMethod());
        Assertions.assertEquals("/api/transactions/all", request.getPath());

        // ASSERT: Response
        assertNotNull(transactions,                  "Transactions list should not be null");
        Assertions.assertEquals(2, transactions.size());

        Assertions.assertEquals("REF001", transactions.get(0).getReferenceId());
        Assertions.assertEquals("ACC123", transactions.get(0).getAccountNumber());
        assertNull(transactions.get(0).getTargetAccount());
        Assertions.assertEquals(new BigDecimal("500.00"), transactions.get(0).getAmount());
        Assertions.assertEquals("DEPOSIT", transactions.get(0).getType());
        Assertions.assertEquals("Cash deposit", transactions.get(0).getNarration());
        Assertions.assertEquals(LocalDateTime.of(2026,1,1,10,0,0), transactions.get(0).getCreatedAt());
        Assertions.assertEquals("teller01", transactions.get(0).getCreatedBy());

        Assertions.assertEquals("REF002", transactions.get(1).getReferenceId());
        Assertions.assertEquals("ACC123", transactions.get(1).getAccountNumber());
        Assertions.assertEquals("ACC456", transactions.get(1).getTargetAccount());
        Assertions.assertEquals(new BigDecimal("200.00"), transactions.get(1).getAmount());
        Assertions.assertEquals("TRANSFER", transactions.get(1).getType());
        Assertions.assertEquals("Transfer to ACC456", transactions.get(1).getNarration());
        Assertions.assertEquals(LocalDateTime.of(2026,1,2,11,0,0), transactions.get(1).getCreatedAt());
        Assertions.assertEquals("teller02", transactions.get(1).getCreatedBy());
    }


    // ══════════════════════════════════════════════════════════════════════════
    // TEST 2 — getTransactionsByAccount(accountNumber, page, size)
    // ══════════════════════════════════════════════════════════════════════════
    @Test
    void shouldCallGetTransactionsByAccountWithPaginationAndMapResponse() throws Exception {

        // ── ARRANGE ──────────────────────────────────────────────────────────
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("""
                    [
                      {
                        "referenceId": "REF003",
                        "accountNumber": "ACC123",
                        "targetAccount": null,
                        "amount": 300.00,
                        "type": "WITHDRAWAL",
                        "narration": "ATM withdrawal",
                        "createdAt": "2026-01-03T09:00:00",
                        "createdBy": "teller01"
                      }
                    ]
                """)
        );

        // ACT
        List<TransactionDto> transactions = transactionClient.getTransactionsByAccount("ACC123", 0, 10);
        RecordedRequest request = mockWebServer.takeRequest(3, TimeUnit.SECONDS);

        //  ASSERT: Request
        assertNotNull(request,                       "No request received by MockWebServer");
        Assertions.assertEquals("GET", request.getMethod());
        Assertions.assertEquals("/api/transactions/account/ACC123?page=0&size=10", request.getPath());

        // ASSERT: Response
        assertNotNull(transactions,                  "Transactions list should not be null");
        Assertions.assertEquals(1, transactions.size());

        Assertions.assertEquals("REF003", transactions.get(0).getReferenceId());
        Assertions.assertEquals("ACC123", transactions.get(0).getAccountNumber());
        assertNull(transactions.get(0).getTargetAccount());
        Assertions.assertEquals(new BigDecimal("300.00"), transactions.get(0).getAmount());
        Assertions.assertEquals("WITHDRAWAL", transactions.get(0).getType());
        Assertions.assertEquals("ATM withdrawal", transactions.get(0).getNarration());
        Assertions.assertEquals(LocalDateTime.of(2026,1,3,9,0,0), transactions.get(0).getCreatedAt());
        Assertions.assertEquals("teller01", transactions.get(0).getCreatedBy());
    }


    //
    // TEST 3 — getTransactionsByAccountAndDateRange(accountNumber, startDate, endDate)
    //
    @Test
    void shouldCallGetTransactionsByDateRangeAndMapResponse() throws Exception {

        // ARRANGE
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("""
                    [
                      {
                        "referenceId": "REF004",
                        "accountNumber": "ACC123",
                        "targetAccount": null,
                        "amount": 1000.00,
                        "type": "DEPOSIT",
                        "narration": "Salary payment",
                        "createdAt": "2026-01-15T08:00:00",
                        "createdBy": "teller03"
                      }
                    ]
                """)
        );

        // ACT
        List<TransactionDto> transactions = transactionClient.getTransactionsByAccountAndDateRange(
                "ACC123",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );
        RecordedRequest request = mockWebServer.takeRequest(3, TimeUnit.SECONDS);

        //  ASSERT: Request
        assertNotNull(request,                       "No request received by MockWebServer");
        Assertions.assertEquals("GET", request.getMethod());
        Assertions.assertEquals("/api/transactions/account/ACC123/range?startDate=2026-01-01&endDate=2026-01-31", request.getPath());

        // ASSERT: Response
        assertNotNull(transactions,                  "Transactions list should not be null");
        Assertions.assertEquals(1, transactions.size());

        Assertions.assertEquals("REF004", transactions.get(0).getReferenceId());
        Assertions.assertEquals("ACC123", transactions.get(0).getAccountNumber());
        assertNull(transactions.get(0).getTargetAccount());
        Assertions.assertEquals(new BigDecimal("1000.00"), transactions.get(0).getAmount());
        Assertions.assertEquals("DEPOSIT", transactions.get(0).getType());
        Assertions.assertEquals("Salary payment", transactions.get(0).getNarration());
        Assertions.assertEquals(LocalDateTime.of(2026,1,15,8,0,0), transactions.get(0).getCreatedAt());
        Assertions.assertEquals("teller03", transactions.get(0).getCreatedBy());
    }
}
package com.steve.audit_service.feign;

import com.steve.audit_service.audit.AccountType;
import com.steve.audit_service.audit.AuditTrailRepository;
import com.steve.audit_service.reporting.client.AccountClient;
import com.steve.audit_service.reporting.dto.AccountDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
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
@EnableFeignClients(clients = AccountClient.class)
@ActiveProfiles("test")
class AccountClientContractTest {

    static MockWebServer mockWebServer;

    // ✅ START MOCK SERVER BEFORE SPRING CONTEXT
    static {
        try {
            mockWebServer = new MockWebServer();
            mockWebServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private AccountClient accountClient;


    @TestConfiguration
    static class TestConfig {
        @Bean
        AuditTrailRepository auditTrailRepository() {
            return Mockito.mock(AuditTrailRepository.class);
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "corebanking.service.url",
                () -> mockWebServer.url("/").toString()
        );
    }
    @BeforeEach
    void printMockServerUrl() {
        System.out.println(">>> MockWebServer URL: http://localhost:" + mockWebServer.getPort());
        System.out.println(">>> MockWebServer running: " + !mockWebServer.getHostName().isEmpty());
    }


    @AfterAll
    static void shutdown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldCallGetAccountEndpointAndMapResponse() throws Exception {
        try {
            mockWebServer.enqueue(
                    new MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody("""
                                        {
                                          "accountNumber": "ACC123",
                                          "accountType": "SAVINGS",
                                          "balance": 1000.00
                                        }
                                    """)
            );

            AccountDto account = accountClient.getAccount("ACC123");
            System.out.println(">>> Account received: " + account);

            RecordedRequest request = mockWebServer.takeRequest(3, TimeUnit.SECONDS);
            System.out.println(">>> Request path: " + (request != null ? request.getPath() : "NULL - no request received"));

            assertNotNull(request, "No request received by MockWebServer");
            assertEquals("GET", request.getMethod());
            assertEquals("/api/accounts/ACC123", request.getPath());
            assertEquals("ACC123", account.getAccountNumber());
            assertEquals(AccountType.SAVINGS, account.getAccountType());
            assertEquals(new BigDecimal("1000.00"), account.getBalance());

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
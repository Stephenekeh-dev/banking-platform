package com.steve.audit_service.feign;

import com.steve.audit_service.audit.AccountType;
import com.steve.audit_service.reporting.client.AccountClient;
import com.steve.audit_service.reporting.dto.AccountDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@EnableFeignClients(clients = AccountClient.class)
@TestPropertySource(properties = {
        "corebanking.service.url=http://localhost:${mock.server.port}"
})
class AccountClientContractTest {

    static MockWebServer mockWebServer;

    @Autowired
    private AccountClient accountClient;

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        System.setProperty(
                "mock.server.port",
                String.valueOf(mockWebServer.getPort())
        );
    }

    @AfterAll
    static void shutdown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldCallGetAccountEndpointAndMapResponse() throws Exception {
        // Arrange
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

        // Act
        AccountDto account =
                accountClient.getAccount("ACC123");

        // Assert – HTTP contract
        RecordedRequest request = mockWebServer.takeRequest();

        assertEquals("GET", request.getMethod());
        assertEquals("/api/accounts/ACC123", request.getPath());

        // Assert – response mapping
        assertEquals("ACC123", account.getAccountNumber());
        assertEquals(AccountType.SAVINGS, account.getAccountType());
        assertEquals(new BigDecimal("1000.00"), account.getBalance());
    }
}


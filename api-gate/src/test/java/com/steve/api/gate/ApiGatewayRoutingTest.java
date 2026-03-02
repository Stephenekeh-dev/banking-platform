package com.steve.api.gate;


import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.loadbalancer.enabled=false",
                "eureka.client.enabled=false"
        }
)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ApiGatewayRoutingTest {

    @Autowired
    private WebTestClient webTestClient;

    static MockWebServer coreBankingServer;
    static MockWebServer auditServer;

    static {
        try {
            coreBankingServer = new MockWebServer();
            auditServer       = new MockWebServer();
            coreBankingServer.start();
            auditServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("corebanking.mock.port", () -> coreBankingServer.getPort());
        registry.add("audit.mock.port",       () -> auditServer.getPort());
    }

    @AfterAll
    static void shutdown() throws IOException {
        coreBankingServer.shutdown();
        auditServer.shutdown();
    }


    // ══════════════════════════════════════════════════════════════════════════
    // COREBANKING ROUTES
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void shouldRouteAuthRequestToCoreBankingService() throws Exception {

        // ── ARRANGE ──────────────────────────────────────────────────────────
        coreBankingServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("""
                    { "token": "jwt-token-here" }
                """)
        );

        // ── ACT & ASSERT ──────────────────────────────────────────────────────
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                { "username": "admin", "password": "password" }
            """)
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = coreBankingServer.takeRequest(3, TimeUnit.SECONDS);
        assertNotNull(request,          "No request received by corebanking server");
        assertEquals("/api/auth/login", request.getPath());
    }

    @Test
    void shouldRouteAccountRequestToCoreBankingService() throws Exception {

        // ── ARRANGE ──────────────────────────────────────────────────────────
        coreBankingServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("""
                    [
                      { "accountNumber": "ACC123", "accountType": "SAVINGS", "balance": 1000.00 }
                    ]
                """)
        );

        // ── ACT & ASSERT ──────────────────────────────────────────────────────
        webTestClient.get()
                .uri("/api/accounts/all")
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = coreBankingServer.takeRequest(3, TimeUnit.SECONDS);
        assertNotNull(request,           "No request received by corebanking server");
        assertEquals("/api/accounts/all", request.getPath());
    }

    @Test
    void shouldRouteTransactionRequestToCoreBankingService() throws Exception {

        // ── ARRANGE ──────────────────────────────────────────────────────────
        coreBankingServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("[]")
        );

        // ── ACT & ASSERT ──────────────────────────────────────────────────────
        webTestClient.get()
                .uri("/api/transactions/all")
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = coreBankingServer.takeRequest(3, TimeUnit.SECONDS);
        assertNotNull(request,                "No request received by corebanking server");
        assertEquals("/api/transactions/all", request.getPath());
    }

    @Test
    void shouldRouteLedgerRequestToCoreBankingService() throws Exception {

        // ── ARRANGE ──────────────────────────────────────────────────────────
        coreBankingServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("[]")
        );

        // ── ACT & ASSERT ──────────────────────────────────────────────────────
        webTestClient.get()
                .uri("/api/ledger/all/ACC123")
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = coreBankingServer.takeRequest(3, TimeUnit.SECONDS);
        assertNotNull(request,                  "No request received by corebanking server");
        assertEquals("/api/ledger/all/ACC123",  request.getPath());
    }

    @Test
    void shouldRouteCustomerRequestToCoreBankingService() throws Exception {

        // ── ARRANGE ──────────────────────────────────────────────────────────
        coreBankingServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("[]")
        );

        // ── ACT & ASSERT ──────────────────────────────────────────────────────
        webTestClient.get()
                .uri("/api/customers/all")
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = coreBankingServer.takeRequest(3, TimeUnit.SECONDS);
        assertNotNull(request,               "No request received by corebanking server");
        assertEquals("/api/customers/all",   request.getPath());
    }


    // ══════════════════════════════════════════════════════════════════════════
    // AUDIT SERVICE ROUTES
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void shouldRouteAuditRequestToAuditService() throws Exception {

        // ── ARRANGE ──────────────────────────────────────────────────────────
        auditServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("[]")
        );

        // ── ACT & ASSERT ──────────────────────────────────────────────────────
        webTestClient.get()
                .uri("/api/audit/all")
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = auditServer.takeRequest(3, TimeUnit.SECONDS);
        assertNotNull(request,         "No request received by audit server");
        assertEquals("/api/audit/all", request.getPath());
    }

    @Test
    void shouldRouteReportingRequestToAuditService() throws Exception {

        // ── ARRANGE ──────────────────────────────────────────────────────────
        auditServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{}")
        );

        // ── ACT & ASSERT ──────────────────────────────────────────────────────
        webTestClient.get()
                .uri("/api/reports/daily")
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = auditServer.takeRequest(3, TimeUnit.SECONDS);
        assertNotNull(request,            "No request received by audit server");
        assertEquals("/api/reports/daily", request.getPath());
    }


    // ══════════════════════════════════════════════════════════════════════════
    // ACTUATOR
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void shouldExposeHealthEndpoint() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    assertNotNull(response.getResponseBody());
                    Assertions.assertTrue(response.getResponseBody().contains("UP"));
                });
    }

    @Test
    void shouldReturn404ForUnknownRoute() {
        webTestClient.get()
                .uri("/api/unknown/route")
                .exchange()
                .expectStatus().isNotFound();
    }
}
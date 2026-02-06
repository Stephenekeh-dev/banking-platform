package com.steve.audit_service.audit;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_trail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User performing the action
    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String role; // ADMIN, TELLER, AUDITOR

    // Action metadata
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditActionType actionType;
    // e.g. CREATE_ACCOUNT, TRANSFER, LOGIN, FAILED_LOGIN

    @Column(nullable = false)
    private String resourceId;
    // accountNumber, transactionRef, ledgerId

    @Column(nullable = false)
    private String status;
    // SUCCESS, FAILURE

    @Column(columnDefinition = "TEXT")
    private String details;
    // JSON or message payload

    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}

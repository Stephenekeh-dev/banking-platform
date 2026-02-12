package com.steve.audit_service.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditTrailService {

    private final AuditTrailRepository auditTrailRepository;

    /**
     * Log an audit event
     *
     * @param username    the user performing the action
     * @param role        the role of the user (ADMIN, TELLER, AUDITOR)
     * @param actionType  type of action (CREATE_ACCOUNT, TRANSFER, LOGIN, etc.)
     * @param resourceId  target of the action (accountNumber, transactionRef, ledgerId)
     * @param status      SUCCESS or FAILURE
     * @param details     optional JSON/details about the action
     * @param ipAddress   IP address of user
     */
    public void logEvent(
            String username,
            String role,
            AuditActionType actionType,
            String resourceId,
            String status,
            String details,
            String ipAddress
    ) {
        AuditTrail audit = AuditTrail.builder()
                .username(username)
                .role(role)
                .actionType(AuditActionType.LOGIN)
                .resourceId(resourceId)
                .status(status)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();

        auditTrailRepository.save(audit);
    }
}
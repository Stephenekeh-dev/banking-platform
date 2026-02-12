package com.steve.audit_service.auditTest;

import java.time.LocalDateTime;

import com.steve.audit_service.audit.AuditActionType;
import com.steve.audit_service.audit.AuditTrail;
import com.steve.audit_service.audit.AuditTrailRepository;
import com.steve.audit_service.audit.AuditTrailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditTrailServiceTest {

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @InjectMocks
    private AuditTrailService auditTrailService;

    @Test
    void logEvent_ShouldCreateAndSaveAuditTrail() {
        // given
        String username = "john.doe";
        String role = "ADMIN";
        AuditActionType actionType = AuditActionType.LOGIN;
        String resourceId = "N/A";
        String status = "SUCCESS";
        String details = "User logged in successfully";
        String ipAddress = "192.168.1.10";

        // when
        auditTrailService.logEvent(
                username,
                role,
                actionType,
                resourceId,
                status,
                details,
                ipAddress
        );

        // then — capture saved entity
        ArgumentCaptor<AuditTrail> captor =
                ArgumentCaptor.forClass(AuditTrail.class);

        verify(auditTrailRepository).save(captor.capture());

        AuditTrail savedAudit = captor.getValue();

        assertNotNull(savedAudit);
        assertEquals(username, savedAudit.getUsername());
        assertEquals(role, savedAudit.getRole());
        assertEquals(resourceId, savedAudit.getResourceId());
        assertEquals(status, savedAudit.getStatus());
        assertEquals(details, savedAudit.getDetails());
        assertEquals(ipAddress, savedAudit.getIpAddress());

        // timestamp should be set
        assertNotNull(savedAudit.getTimestamp());
        assertTrue(savedAudit.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}
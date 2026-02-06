package com.steve.audit_service.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {

    Page<AuditTrail> findByUsername(String username, Pageable pageable);

    Page<AuditTrail> findByRole(String role, Pageable pageable);

    List<AuditTrail> findByActionType(AuditActionType actionType);

    Page<AuditTrail> findByTimestampBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<AuditTrail> findByUsernameAndTimestampBetween(
            String username,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}

package com.steve.audit_service.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

   private final AuditTrailRepository auditTrailRepository;



    // GET ALL AUDIT LOGS (Paginated)

    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @GetMapping("/logs")
    public ResponseEntity<Page<AuditTrail>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditTrail> logs = auditTrailRepository.findAll(pageable);
        return ResponseEntity.ok(logs);
    }


    // GET AUDIT LOGS BY USER

    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @GetMapping("/user/{username}")
    public ResponseEntity<Page<AuditTrail>> getLogsByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditTrail> logs = auditTrailRepository.findByUsername(username, pageable);
        return ResponseEntity.ok(logs);
    }


    // GET AUDIT LOGS BY ROLE

    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @GetMapping("/role/{role}")
    public ResponseEntity<Page<AuditTrail>> getLogsByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditTrail> logs = auditTrailRepository.findByRole(role, pageable);
        return ResponseEntity.ok(logs);
    }

    // ==========================
    // GET AUDIT LOGS BY DATE RANGE
    // ==========================
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @GetMapping("/range")
    public ResponseEntity<Page<AuditTrail>> getLogsByDateRange(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditTrail> logs = auditTrailRepository.findByTimestampBetween(startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }
}

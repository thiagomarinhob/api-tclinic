package com.jettech.api.solutions_clinic.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface AuditAccessLogRepository extends JpaRepository<AuditAccessLog, UUID> {

    void deleteByAccessedAtBefore(Instant cutoff);

    Page<AuditAccessLog> findByPatientIdOrderByAccessedAtDesc(UUID patientId, Pageable pageable);
}

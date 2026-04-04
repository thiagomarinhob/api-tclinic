package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.Exam;
import com.jettech.api.solutions_clinic.model.entity.ExamStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

    @EntityGraph(attributePaths = {"patient"})
    @Query("""
        SELECT e FROM exams e
        WHERE e.tenant.id = :tenantId
        AND (:patientId IS NULL OR e.patient.id = :patientId)
        AND (:status IS NULL OR e.status = :status)
        """)
    Page<Exam> findByTenantIdWithFilters(
        @Param("tenantId") UUID tenantId,
        @Param("patientId") UUID patientId,
        @Param("status") ExamStatus status,
        Pageable pageable
    );

    List<Exam> findByTenantId(UUID tenantId);

    List<Exam> findByPatientId(UUID patientId);

    List<Exam> findByPatientIdAndTenantId(UUID patientId, UUID tenantId);

    Page<Exam> findByPatientIdAndTenantId(UUID patientId, UUID tenantId, Pageable pageable);

    Page<Exam> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Exam> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Exam> findByAppointmentId(UUID appointmentId);

    List<Exam> findByPatientIdAndTenantIdAndStatus(UUID patientId, UUID tenantId, ExamStatus status);
}

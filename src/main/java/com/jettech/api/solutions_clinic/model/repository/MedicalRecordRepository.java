package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.MedicalRecord;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecord.MedicalRecordListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {

    Optional<MedicalRecord> findByAppointmentId(UUID appointmentId);

    Optional<MedicalRecord> findByIdAndAppointment_TenantId(UUID id, UUID tenantId);

    @Query(
        value = "SELECT new com.jettech.api.solutions_clinic.model.usecase.medicalrecord.MedicalRecordListResponse("
            + "mr.id, a.id, p.firstName, CONCAT(u.firstName, ' ', u.lastName), a.scheduledAt, mr.createdAt, mr.signedAt) "
            + "FROM MedicalRecord mr JOIN mr.appointment a JOIN a.patient p JOIN a.professional pr JOIN pr.user u "
            + "WHERE a.tenant.id = :tenantId "
            + "AND a.professional.id = COALESCE(:professionalId, a.professional.id) "
            + "AND (COALESCE(:patientName, '') = '' OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', TRIM(COALESCE(:patientName, '')), '%'))) "
            + "AND a.scheduledAt >= COALESCE(:dateFrom, a.scheduledAt) "
            + "AND a.scheduledAt <= COALESCE(:dateTo, a.scheduledAt) "
            + "ORDER BY mr.createdAt DESC",
        countQuery = "SELECT COUNT(mr) FROM MedicalRecord mr JOIN mr.appointment a JOIN a.patient p JOIN a.professional pr "
            + "WHERE a.tenant.id = :tenantId "
            + "AND a.professional.id = COALESCE(:professionalId, a.professional.id) "
            + "AND (COALESCE(:patientName, '') = '' OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', TRIM(COALESCE(:patientName, '')), '%'))) "
            + "AND a.scheduledAt >= COALESCE(:dateFrom, a.scheduledAt) "
            + "AND a.scheduledAt <= COALESCE(:dateTo, a.scheduledAt)"
    )
    Page<MedicalRecordListResponse> findPageByTenantAndFilters(
        @Param("tenantId") UUID tenantId,
        @Param("professionalId") UUID professionalId,
        @Param("patientName") String patientName,
        @Param("dateFrom") LocalDateTime dateFrom,
        @Param("dateTo") LocalDateTime dateTo,
        Pageable pageable
    );
}

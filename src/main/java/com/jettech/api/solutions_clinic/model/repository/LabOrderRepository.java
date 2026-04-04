package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.LabOrder;
import com.jettech.api.solutions_clinic.model.entity.LabOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LabOrderRepository extends JpaRepository<LabOrder, UUID> {

    @Query("""
        SELECT o FROM lab_orders o
        WHERE o.tenant.id = :tenantId
        AND (:patientId IS NULL OR o.patient.id = :patientId)
        AND (:status IS NULL OR o.status = :status)
        AND (:search IS NULL OR :search = '' OR
             LOWER(COALESCE(o.patient.firstName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(COALESCE(o.sampleCode, '')) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY o.createdAt DESC
        """)
    Page<LabOrder> findByTenantWithFilters(
        @Param("tenantId") UUID tenantId,
        @Param("patientId") UUID patientId,
        @Param("status") LabOrderStatus status,
        @Param("search") String search,
        Pageable pageable
    );

    List<LabOrder> findByPatientIdAndTenantIdOrderByCreatedAtDesc(UUID patientId, UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, LabOrderStatus status);
}

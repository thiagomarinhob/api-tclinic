package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.LabExamType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface LabExamTypeRepository extends JpaRepository<LabExamType, UUID> {

    @Query("""
        SELECT t FROM lab_exam_types t
        WHERE t.tenant.id = :tenantId
        AND (:search IS NULL OR :search = '' OR
             LOWER(COALESCE(t.name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(COALESCE(t.code, '')) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:active IS NULL OR t.active = :active)
        ORDER BY t.name ASC
        """)
    Page<LabExamType> findByTenantWithFilters(
        @Param("tenantId") UUID tenantId,
        @Param("search") String search,
        @Param("active") Boolean active,
        Pageable pageable
    );
}

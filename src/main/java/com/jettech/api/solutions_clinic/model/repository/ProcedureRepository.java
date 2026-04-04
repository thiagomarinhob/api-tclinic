package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.Procedure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcedureRepository extends JpaRepository<Procedure, UUID> {

    List<Procedure> findByTenantId(UUID tenantId);

    List<Procedure> findByTenantIdAndActive(UUID tenantId, boolean active);

    Optional<Procedure> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<Procedure> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Procedure> findByTenantIdAndActive(UUID tenantId, boolean active, Pageable pageable);

    @Query("""
        SELECT p FROM procedures p
        WHERE p.tenant.id = :tenantId
        AND (:professionalId IS NULL OR p.professional.id = :professionalId)
        AND (:search IS NULL OR :search = '' OR
             LOWER(COALESCE(p.name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:active IS NULL OR p.active = :active)
        """)
    Page<Procedure> findByTenantIdWithFilters(
        @Param("tenantId") UUID tenantId,
        @Param("professionalId") UUID professionalId,
        @Param("search") String search,
        @Param("active") Boolean active,
        Pageable pageable
    );

    List<Procedure> findAllByTenantIdAndProfessionalId(UUID tenantId, UUID professionalId);
}

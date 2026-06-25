package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByCpfAndTenantId(String cpf, UUID tenantId);

    List<Patient> findByTenantId(UUID tenantId);

    List<Patient> findByTenantIdAndActive(UUID tenantId, boolean active);

    boolean existsByCpfAndTenantId(String cpf, UUID tenantId);

    Page<Patient> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Patient> findByTenantIdAndActive(UUID tenantId, boolean active, Pageable pageable);

    @Query("""
        SELECT p FROM patients p
        WHERE p.tenant.id = :tenantId
        AND (:search IS NULL OR :search = '' OR
             LOWER(COALESCE(p.firstName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
             p.cpf LIKE CONCAT('%', :search, '%') OR
             p.phone LIKE CONCAT('%', :search, '%') OR
             p.whatsapp LIKE CONCAT('%', :search, '%') OR
             LOWER(COALESCE(p.email, '')) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:active IS NULL OR p.active = :active)
        """)
    Page<Patient> findByTenantIdWithFilters(
        @Param("tenantId") UUID tenantId,
        @Param("search") String search,
        @Param("active") Boolean active,
        Pageable pageable
    );

    /**
     * Busca pacientes cujo WhatsApp coincide com o número normalizado.
     * Aceita números com ou sem DDI 55 para cobrir os dois formatos de cadastro:
     *   normalizedPhone  = "5511999999999" (com DDI, vindo do webhook)
     *   localPhone       = "11999999999"   (sem DDI, como muitos usuários cadastram)
     */
    @Query(value = "SELECT * FROM patients WHERE REGEXP_REPLACE(COALESCE(whatsapp, ''), '[^0-9]', '', 'g') IN (:normalizedPhone, :localPhone)", nativeQuery = true)
    List<Patient> findByWhatsappNormalized(
            @Param("normalizedPhone") String normalizedPhone,
            @Param("localPhone") String localPhone);
}


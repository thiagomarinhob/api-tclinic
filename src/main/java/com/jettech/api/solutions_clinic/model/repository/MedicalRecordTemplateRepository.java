package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.MedicalRecordTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordTemplateRepository extends JpaRepository<MedicalRecordTemplate, UUID> {

    List<MedicalRecordTemplate> findByTenantId(UUID tenantId);

    List<MedicalRecordTemplate> findByTenantIdAndActive(UUID tenantId, boolean active);

    Optional<MedicalRecordTemplate> findByIdAndTenantId(UUID id, UUID tenantId);

    List<MedicalRecordTemplate> findByTenantIdAndProfessionalTypeAndActive(
            UUID tenantId, String professionalType, boolean active);

    /**
     * Templates disponíveis para o tenant: globais + da clínica (professional_id NULL) + do profissional quando informado.
     * professionalId null = só globais e da clínica; com professionalId = também templates desse profissional.
     */
    @Query("""
        SELECT t FROM MedicalRecordTemplate t
        WHERE t.active = true
          AND (t.tenant IS NULL OR t.tenant.id = :tenantId)
          AND (t.professional IS NULL OR (:professionalId IS NOT NULL AND t.professional.id = :professionalId))
        ORDER BY t.tenant.id NULLS FIRST, t.professional.id NULLS FIRST, t.name
        """)
    List<MedicalRecordTemplate> findAvailableForTenant(
            @Param("tenantId") UUID tenantId,
            @Param("professionalId") UUID professionalId);

    /**
     * Mesmo que findAvailableForTenant, filtrado por professional_type.
     * Modelos do próprio profissional (t.professional.id = professionalId) são sempre incluídos,
     * independente do professionalType, para aparecerem no atendimento.
     */
    @Query("""
        SELECT t FROM MedicalRecordTemplate t
        WHERE t.active = true
          AND (t.tenant IS NULL OR t.tenant.id = :tenantId)
          AND (t.professional IS NULL OR (:professionalId IS NOT NULL AND t.professional.id = :professionalId))
          AND (
            (t.professionalType IS NULL OR t.professionalType = :professionalType)
            OR (:professionalId IS NOT NULL AND t.professional IS NOT NULL AND t.professional.id = :professionalId)
          )
        ORDER BY t.tenant.id NULLS FIRST, t.professional.id NULLS FIRST, t.name
        """)
    List<MedicalRecordTemplate> findAvailableForTenantAndProfessionalType(
            @Param("tenantId") UUID tenantId,
            @Param("professionalId") UUID professionalId,
            @Param("professionalType") String professionalType);

    /**
     * Busca por ID visível ao tenant: template global OU da clínica OU do profissional (quando professionalId informado).
     */
    @Query("""
        SELECT t FROM MedicalRecordTemplate t
        WHERE t.id = :id
          AND (t.tenant IS NULL OR t.tenant.id = :tenantId)
          AND (t.professional IS NULL OR (:professionalId IS NOT NULL AND t.professional.id = :professionalId))
        """)
    Optional<MedicalRecordTemplate> findByIdAvailableForTenant(
            @Param("id") UUID id,
            @Param("tenantId") UUID tenantId,
            @Param("professionalId") UUID professionalId);

}

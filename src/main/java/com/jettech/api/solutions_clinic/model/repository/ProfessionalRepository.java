package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.DocumentType;
import com.jettech.api.solutions_clinic.model.entity.Professional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalRepository extends JpaRepository<Professional, UUID> {
    
    Optional<Professional> findByUserIdAndTenantId(UUID userId, UUID tenantId);
    
    Optional<Professional> findByIdAndTenantId(UUID id, UUID tenantId);
    
    List<Professional> findByTenantId(UUID tenantId);
    
    @Query("SELECT DISTINCT p FROM professionals p JOIN FETCH p.user JOIN FETCH p.tenant WHERE p.tenant.id = :tenantId")
    List<Professional> findByTenantIdWithUserAndTenant(@Param("tenantId") UUID tenantId);
    
    List<Professional> findByUserId(UUID userId);
    
    List<Professional> findByActive(boolean active);
    
    // Query otimizada com busca textual e filtros
    // Nota: JOIN FETCH não funciona bem com Pageable, então fazemos JOIN normal
    // e o Hibernate fará fetch lazy dentro da transação
    // COALESCE trata valores NULL em firstName e lastName
    // Removido DISTINCT pois a relação ManyToOne não gera duplicatas e causa problema com ORDER BY
    @Query("""
        SELECT p FROM professionals p 
        JOIN p.user u 
        JOIN p.tenant t 
        WHERE p.tenant.id = :tenantId
        AND (:search IS NULL OR :search = '' OR 
             LOWER(CONCAT(COALESCE(u.firstName, ''), ' ', COALESCE(u.lastName, ''))) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(p.specialty) LIKE LOWER(CONCAT('%', :search, '%')) OR
             p.documentNumber LIKE CONCAT('%', :search, '%') OR
             LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:active IS NULL OR p.active = :active)
        AND (:documentType IS NULL OR p.documentType = :documentType)
        """)
    Page<Professional> findByTenantIdWithFilters(
        @Param("tenantId") UUID tenantId,
        @Param("search") String search,
        @Param("active") Boolean active,
        @Param("documentType") DocumentType documentType,
        Pageable pageable
    );
}


package com.jettech.api.solutions_clinic.model.repository;


import com.jettech.api.solutions_clinic.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    
    Optional<User> findByCpf(String cpf);
    
    @Query("""
        SELECT DISTINCT u FROM users u 
        WHERE u.id IN (SELECT utr.user.id FROM user_tenant_role utr WHERE utr.tenant.id = :tenantId
            AND (:role IS NULL OR utr.role = :role))
        AND (:search IS NULL OR :search = '' OR 
             LOWER(CONCAT(COALESCE(u.firstName, ''), ' ', COALESCE(u.lastName, ''))) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
             u.phone LIKE CONCAT('%', :search, '%') OR
             u.cpf LIKE CONCAT('%', :search, '%'))
        AND (:blocked IS NULL OR u.blocked = :blocked)
        """)
    Page<User> findUsersByTenantIdWithFilters(
        @Param("tenantId") UUID tenantId,
        @Param("search") String search,
        @Param("blocked") Boolean blocked,
        @Param("role") com.jettech.api.solutions_clinic.model.entity.Role role,
        Pageable pageable
    );
}

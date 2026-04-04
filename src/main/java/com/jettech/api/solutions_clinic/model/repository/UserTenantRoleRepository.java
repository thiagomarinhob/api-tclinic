package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.Role;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.entity.UserTenantRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTenantRoleRepository extends JpaRepository<UserTenantRole, UUID> {
    
    List<UserTenantRole> findByUser(User user);
    
    List<UserTenantRole> findByTenant(Tenant tenant);
    
    List<UserTenantRole> findByUserAndTenant(User user, Tenant tenant);
    
    Optional<UserTenantRole> findByUserAndTenantAndRole(User user, Tenant tenant, Role role);
    
    List<UserTenantRole> findByUser_Id(UUID userId);
    
    List<UserTenantRole> findByTenant_Id(UUID tenantId);
    
    List<UserTenantRole> findByUser_IdAndTenant_Id(UUID userId, UUID tenantId);
    
    boolean existsByUserAndTenantAndRole(User user, Tenant tenant, Role role);
}


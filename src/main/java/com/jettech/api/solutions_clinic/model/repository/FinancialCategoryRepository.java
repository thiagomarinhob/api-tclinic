package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.FinancialCategory;
import com.jettech.api.solutions_clinic.model.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialCategoryRepository extends JpaRepository<FinancialCategory, UUID> {
    
    List<FinancialCategory> findByTenantId(UUID tenantId);
    
    List<FinancialCategory> findByTenantIdAndActive(UUID tenantId, boolean active);
    
    List<FinancialCategory> findByTenantIdAndType(UUID tenantId, TransactionType type);
    
    List<FinancialCategory> findByTenantIdAndTypeAndActive(UUID tenantId, TransactionType type, boolean active);
    
    Optional<FinancialCategory> findByNameAndTenantId(String name, UUID tenantId);
    
    boolean existsByNameAndTenantId(String name, UUID tenantId);
}

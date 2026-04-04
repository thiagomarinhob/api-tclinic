package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.HealthPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HealthPlanRepository extends JpaRepository<HealthPlan, UUID> {
    List<HealthPlan> findByTenantIdOrderByNameAsc(UUID tenantId);
    List<HealthPlan> findByTenantIdAndActiveTrueOrderByNameAsc(UUID tenantId);
    Optional<HealthPlan> findByIdAndTenantId(UUID id, UUID tenantId);
}

package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.PlanType;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import org.springframework.data.jpa.domain.Specification;

public class TenantSpecifications {

    private TenantSpecifications() {}

    public static Specification<Tenant> byStatus(TenantStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Tenant> byPlanType(PlanType planType) {
        return (root, query, cb) ->
                planType == null ? cb.conjunction() : cb.equal(root.get("planType"), planType);
    }
}

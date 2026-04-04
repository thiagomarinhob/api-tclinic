package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.LabOrderItem;
import com.jettech.api.solutions_clinic.model.entity.LabResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LabOrderItemRepository extends JpaRepository<LabOrderItem, UUID> {
    List<LabOrderItem> findByOrderId(UUID orderId);
    long countByOrder_Tenant_IdAndResultStatus(UUID tenantId, LabResultStatus resultStatus);
}

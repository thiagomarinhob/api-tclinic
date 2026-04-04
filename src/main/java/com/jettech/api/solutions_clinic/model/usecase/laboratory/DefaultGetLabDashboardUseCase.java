package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.entity.LabOrderStatus;
import com.jettech.api.solutions_clinic.model.entity.LabResultStatus;
import com.jettech.api.solutions_clinic.model.repository.LabOrderItemRepository;
import com.jettech.api.solutions_clinic.model.repository.LabOrderRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetLabDashboardUseCase implements GetLabDashboardUseCase {

    private final LabOrderRepository labOrderRepository;
    private final LabOrderItemRepository labOrderItemRepository;
    private final TenantContext tenantContext;

    @Override
    public LabDashboardResponse execute() throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        long requested  = labOrderRepository.countByTenantIdAndStatus(tenantId, LabOrderStatus.REQUESTED);
        long collected  = labOrderRepository.countByTenantIdAndStatus(tenantId, LabOrderStatus.COLLECTED);
        long inAnalysis = labOrderRepository.countByTenantIdAndStatus(tenantId, LabOrderStatus.IN_ANALYSIS);
        long completed  = labOrderRepository.countByTenantIdAndStatus(tenantId, LabOrderStatus.COMPLETED);
        long pending    = labOrderItemRepository.countByOrder_Tenant_IdAndResultStatus(tenantId, LabResultStatus.PENDING);
        long awaiting   = labOrderItemRepository.countByOrder_Tenant_IdAndResultStatus(tenantId, LabResultStatus.ENTERED);
        return new LabDashboardResponse(requested, collected, inAnalysis, completed, pending, awaiting);
    }
}

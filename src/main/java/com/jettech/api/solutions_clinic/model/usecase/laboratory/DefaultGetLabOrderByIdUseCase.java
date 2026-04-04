package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.LabOrder;
import com.jettech.api.solutions_clinic.model.repository.LabOrderRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetLabOrderByIdUseCase implements GetLabOrderByIdUseCase {

    private final LabOrderRepository labOrderRepository;
    private final TenantContext tenantContext;

    @Override
    public LabOrderResponse execute(UUID id) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        LabOrder order = labOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido laboratorial", id));
        if (!order.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }
        return DefaultCreateLabOrderUseCase.toResponse(order);
    }
}

package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.LabOrder;
import com.jettech.api.solutions_clinic.model.repository.LabOrderRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetLabOrderByIdUseCase implements GetLabOrderByIdUseCase {

    private final LabOrderRepository labOrderRepository;
    private final TenantContext tenantContext;

    @Override
    public LabOrderResponse execute(UUID id) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        log.info("Buscando pedido laboratorial por id: {}, tenantId: {}", id, tenantId);
        LabOrder order = labOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido laboratorial", id));
        if (!order.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao pedido lab {} - tenantId: {}", id, tenantId);
            throw new ForbiddenException();
        }
        log.info("Pedido laboratorial {} encontrado - status: {}", id, order.getStatus());
        return DefaultCreateLabOrderUseCase.toResponse(order);
    }
}

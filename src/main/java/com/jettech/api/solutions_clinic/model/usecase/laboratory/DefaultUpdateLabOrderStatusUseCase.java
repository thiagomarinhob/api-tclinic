package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.LabOrder;
import com.jettech.api.solutions_clinic.model.entity.LabOrderStatus;
import com.jettech.api.solutions_clinic.model.repository.LabOrderRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultUpdateLabOrderStatusUseCase implements UpdateLabOrderStatusUseCase {

    private final LabOrderRepository labOrderRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public LabOrderResponse execute(UpdateLabOrderStatusRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        log.info("Atualizando status do pedido lab - orderId: {}, novoStatus: {}, tenantId: {}",
                request.id(), request.status(), tenantId);
        LabOrder order = labOrderRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Pedido laboratorial", request.id()));
        if (!order.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao atualizar status do pedido lab {} - tenantId: {}", request.id(), tenantId);
            throw new ForbiddenException();
        }
        LabOrderStatus statusAnterior = order.getStatus();
        order.setStatus(request.status());
        if (request.sampleCode() != null) order.setSampleCode(request.sampleCode());
        if (request.collectedBy() != null) order.setCollectedBy(request.collectedBy());
        if (request.collectedAt() != null) {
            order.setCollectedAt(request.collectedAt());
        } else if (request.status() == LabOrderStatus.COLLECTED && order.getCollectedAt() == null) {
            order.setCollectedAt(LocalDateTime.now());
        }
        if (request.receivedAt() != null) {
            order.setReceivedAt(request.receivedAt());
        } else if (request.status() == LabOrderStatus.RECEIVED && order.getReceivedAt() == null) {
            order.setReceivedAt(LocalDateTime.now());
        }
        order = labOrderRepository.save(order);
        log.info("Status do pedido lab atualizado - orderId: {}, statusAnterior: {}, novoStatus: {}",
                order.getId(), statusAnterior, order.getStatus());
        return DefaultCreateLabOrderUseCase.toResponse(order);
    }
}

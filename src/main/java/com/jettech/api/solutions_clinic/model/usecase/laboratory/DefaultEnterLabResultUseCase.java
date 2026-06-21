package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.LabOrderItem;
import com.jettech.api.solutions_clinic.model.entity.LabResultStatus;
import com.jettech.api.solutions_clinic.model.repository.LabOrderItemRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultEnterLabResultUseCase implements EnterLabResultUseCase {

    private final LabOrderItemRepository labOrderItemRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public LabOrderItemResponse execute(EnterLabResultRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        log.info("Inserindo resultado de exame lab - itemId: {}, tenantId: {}, abnormal: {}, critical: {}",
                request.itemId(), tenantId, request.abnormal(), request.critical());
        LabOrderItem item = labOrderItemRepository.findById(request.itemId())
                .orElseThrow(() -> new EntityNotFoundException("Item do pedido", request.itemId()));
        if (!item.getOrder().getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao inserir resultado - item {} não pertence ao tenantId: {}", request.itemId(), tenantId);
            throw new ForbiddenException();
        }
        item.setResultValue(request.resultValue());
        item.setAbnormal(request.abnormal());
        item.setCritical(request.critical());
        item.setObservations(request.observations());
        item.setResultStatus(LabResultStatus.ENTERED);
        item = labOrderItemRepository.save(item);
        log.info("Resultado inserido - itemId: {}, status: {}, abnormal: {}, critical: {}",
                item.getId(), item.getResultStatus(), item.getAbnormal(), item.isCritical());
        return DefaultCreateLabOrderUseCase.toItemResponse(item);
    }
}

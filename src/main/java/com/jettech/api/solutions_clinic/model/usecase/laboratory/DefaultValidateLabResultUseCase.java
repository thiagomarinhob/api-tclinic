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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultValidateLabResultUseCase implements ValidateLabResultUseCase {

    private final LabOrderItemRepository labOrderItemRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public LabOrderItemResponse execute(ValidateLabResultRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        LabOrderItem item = labOrderItemRepository.findById(request.itemId())
                .orElseThrow(() -> new EntityNotFoundException("Item do pedido", request.itemId()));
        if (!item.getOrder().getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }
        if (request.validationType() == ValidateLabResultBodyRequest.ValidationType.TECHNICAL) {
            item.setTechnicalValidatedBy(request.validatedBy());
            item.setTechnicalValidatedAt(LocalDateTime.now());
            item.setResultStatus(LabResultStatus.TECHNICAL_VALIDATED);
        } else {
            item.setFinalValidatedBy(request.validatedBy());
            item.setFinalValidatedAt(LocalDateTime.now());
            item.setResultStatus(LabResultStatus.RELEASED);
        }
        item = labOrderItemRepository.save(item);
        return DefaultCreateLabOrderUseCase.toItemResponse(item);
    }
}

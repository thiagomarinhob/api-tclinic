package com.jettech.api.solutions_clinic.model.usecase.tenant;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultUpdateConfirmationWindowUseCase implements UpdateConfirmationWindowUseCase {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public TenantResponse execute(UpdateConfirmationWindowRequest request) throws AuthenticationFailedException {
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.tenantId()));

        tenant.setConfirmationWindowMinutes(request.confirmationWindowMinutes());
        tenant = tenantRepository.save(tenant);

        log.info("Janela de confirmação atualizada - tenantId: {}, confirmationWindowMinutes: {}",
                tenant.getId(), tenant.getConfirmationWindowMinutes());

        return toResponse(tenant);
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getCnpj(),
                tenant.getPlanType(),
                tenant.getAddress(),
                tenant.getPhone(),
                tenant.isActive(),
                tenant.getSubdomain(),
                tenant.getType(),
                tenant.getStatus(),
                tenant.getTrialEndsAt(),
                tenant.getLogoObjectKey(),
                tenant.getConfirmationWindowMinutes(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}

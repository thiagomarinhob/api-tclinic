package com.jettech.api.solutions_clinic.model.usecase.tenant;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.InvalidStateException;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultExtendTrialUseCase implements ExtendTrialUseCase {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public TenantResponse execute(ExtendTrialRequest request) throws AuthenticationFailedException {
        log.warn("EXTENSAO DE TRIAL - tenantId: {}, additionalDays: {}", request.tenantId(), request.additionalDays());

        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.tenantId()));

        validateStatus(tenant);

        LocalDate base = resolveBase(tenant);
        LocalDate newTrialEndsAt = base.plusDays(request.additionalDays());

        tenant.setTrialEndsAt(newTrialEndsAt);
        tenant.setStatus(TenantStatus.TRIAL);
        tenant.setActive(true);

        tenant = tenantRepository.save(tenant);

        log.info("Trial estendido - tenantId: {}, novoTrialEndsAt: {}", tenant.getId(), tenant.getTrialEndsAt());

        return toResponse(tenant);
    }

    private void validateStatus(Tenant tenant) {
        if (tenant.getStatus() == TenantStatus.ACTIVE) {
            log.warn("Tentativa de estender trial de tenant ACTIVE - tenantId: {}", tenant.getId());
            throw new InvalidStateException(ApiError.INVALID_STATE_EXTEND_TRIAL_ACTIVE);
        }
        if (tenant.getStatus() == TenantStatus.CANCELED) {
            log.warn("Tentativa de estender trial de tenant CANCELED - tenantId: {}", tenant.getId());
            throw new InvalidStateException(ApiError.INVALID_STATE_EXTEND_TRIAL_CANCELED);
        }
    }

    /** Se já há trial em andamento, estende a partir da data atual; caso contrário usa hoje. */
    private LocalDate resolveBase(Tenant tenant) {
        LocalDate today = LocalDate.now();
        if (tenant.getStatus() == TenantStatus.TRIAL
                && tenant.getTrialEndsAt() != null
                && tenant.getTrialEndsAt().isAfter(today)) {
            return tenant.getTrialEndsAt();
        }
        return today;
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
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}

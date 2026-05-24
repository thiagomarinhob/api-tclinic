package com.jettech.api.solutions_clinic.model.usecase.consent;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.DuplicateEntityException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.PatientConsent;
import com.jettech.api.solutions_clinic.model.repository.PatientConsentRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultRevokeConsentUseCase implements RevokeConsentUseCase {

    private final PatientConsentRepository patientConsentRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public PatientConsentResponse execute(RevokeConsentRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();

        PatientConsent consent = patientConsentRepository.findById(request.consentId())
                .orElseThrow(() -> new EntityNotFoundException("Consentimento", request.consentId()));

        if (!consent.getPatient().getId().equals(request.patientId())
                || !consent.getPatient().getTenant().getId().equals(tenantId)) {
            throw new EntityNotFoundException("Consentimento", request.consentId());
        }

        if (consent.getRevokedAt() != null) {
            throw new DuplicateEntityException(ApiError.CONSENT_ALREADY_REVOKED);
        }

        consent.setRevokedAt(LocalDateTime.now());
        consent = patientConsentRepository.save(consent);

        return DefaultGrantConsentUseCase.toResponse(consent);
    }
}

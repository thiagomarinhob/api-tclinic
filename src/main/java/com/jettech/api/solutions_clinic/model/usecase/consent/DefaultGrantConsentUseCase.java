package com.jettech.api.solutions_clinic.model.usecase.consent;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.entity.PatientConsent;
import com.jettech.api.solutions_clinic.model.repository.PatientConsentRepository;
import com.jettech.api.solutions_clinic.model.repository.PatientRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGrantConsentUseCase implements GrantConsentUseCase {

    private final PatientRepository patientRepository;
    private final PatientConsentRepository patientConsentRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public PatientConsentResponse execute(GrantConsentRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente", request.patientId()));

        if (!patient.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }

        PatientConsent consent = new PatientConsent();
        consent.setPatient(patient);
        consent.setConsentType(request.consentType());
        consent.setGranted(true);
        consent.setGrantedAt(LocalDateTime.now());
        consent.setTermVersion(request.termVersion());
        consent.setIpAddress(request.ipAddress());

        consent = patientConsentRepository.save(consent);

        return toResponse(consent);
    }

    static PatientConsentResponse toResponse(PatientConsent consent) {
        return new PatientConsentResponse(
                consent.getId(),
                consent.getPatient().getId(),
                consent.getConsentType(),
                consent.isGranted(),
                consent.getGrantedAt(),
                consent.getRevokedAt(),
                consent.getIpAddress(),
                consent.getTermVersion()
        );
    }
}

package com.jettech.api.solutions_clinic.model.usecase.consent;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.repository.PatientConsentRepository;
import com.jettech.api.solutions_clinic.model.repository.PatientRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetPatientConsentsUseCase implements GetPatientConsentsUseCase {

    private final PatientRepository patientRepository;
    private final PatientConsentRepository patientConsentRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public List<PatientConsentResponse> execute(GetPatientConsentsRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente", request.patientId()));

        if (!patient.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }

        var consents = request.consentType() != null
                ? patientConsentRepository.findByPatientIdAndConsentTypeOrderByGrantedAtDesc(request.patientId(), request.consentType())
                : patientConsentRepository.findByPatientIdOrderByGrantedAtDesc(request.patientId());

        return consents.stream()
                .map(DefaultGrantConsentUseCase::toResponse)
                .toList();
    }
}

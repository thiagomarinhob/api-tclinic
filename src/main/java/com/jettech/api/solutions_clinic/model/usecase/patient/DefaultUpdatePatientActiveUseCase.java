package com.jettech.api.solutions_clinic.model.usecase.patient;

import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.repository.PatientRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultUpdatePatientActiveUseCase implements UpdatePatientActiveUseCase {

    private final PatientRepository patientRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public PatientResponse execute(UpdatePatientActiveRequest request) throws AuthenticationFailedException {
        Patient patient = patientRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Paciente", request.id()));
        if (!patient.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        patient.setActive(request.active());
        patient = patientRepository.save(patient);

        return new PatientResponse(
                patient.getId(),
                patient.getTenant().getId(),
                patient.getFirstName(),
                patient.getMotherName(),
                patient.getCpf(),
                patient.getBirthDate(),
                patient.getGender(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getWhatsapp(),
                patient.getAddressStreet(),
                patient.getAddressNumber(),
                patient.getAddressComplement(),
                patient.getAddressNeighborhood(),
                patient.getAddressCity(),
                patient.getAddressState(),
                patient.getAddressZipcode(),
                patient.getBloodType(),
                patient.getAllergies(),
                patient.getHealthPlan(),
                patient.getGuardianName(),
                patient.getGuardianPhone(),
                patient.getGuardianRelationship(),
                patient.isActive(),
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }
}

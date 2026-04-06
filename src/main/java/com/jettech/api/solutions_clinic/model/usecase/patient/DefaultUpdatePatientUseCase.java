package com.jettech.api.solutions_clinic.model.usecase.patient;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.DuplicateEntityException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.repository.PatientRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultUpdatePatientUseCase implements UpdatePatientUseCase {

    private final PatientRepository patientRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public PatientResponse execute(UpdatePatientRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();

        Patient patient = patientRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Paciente", request.id()));

        if (!patient.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }

        if (request.cpf() != null && !request.cpf().isEmpty()) {
            patientRepository.findByCpfAndTenantId(request.cpf(), tenantId)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(request.id())) {
                            throw new DuplicateEntityException(ApiError.DUPLICATE_PATIENT_CPF);
                        }
                    });
        }

        patient.setFirstName(request.firstName());
        patient.setMotherName(request.motherName());
        patient.setCpf(request.cpf());
        patient.setBirthDate(request.birthDate());
        patient.setGender(request.gender());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());
        patient.setWhatsapp(request.whatsapp());
        patient.setAddressStreet(request.addressStreet());
        patient.setAddressNumber(request.addressNumber());
        patient.setAddressComplement(request.addressComplement());
        patient.setAddressNeighborhood(request.addressNeighborhood());
        patient.setAddressCity(request.addressCity());
        patient.setAddressState(request.addressState());
        patient.setAddressZipcode(request.addressZipcode());
        patient.setBloodType(request.bloodType());
        patient.setAllergies(request.allergies());
        patient.setHealthPlan(request.healthPlan());
        patient.setGuardianName(request.guardianName());
        patient.setGuardianPhone(request.guardianPhone());
        patient.setGuardianRelationship(request.guardianRelationship());

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

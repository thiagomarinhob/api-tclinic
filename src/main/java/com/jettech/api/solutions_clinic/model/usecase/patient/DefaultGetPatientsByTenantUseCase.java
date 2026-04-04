package com.jettech.api.solutions_clinic.model.usecase.patient;

import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.repository.PatientRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.security.TenantContext;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetPatientsByTenantUseCase implements GetPatientsByTenantUseCase {

    private final PatientRepository patientRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponse> execute(GetPatientsByTenantRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());
        tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.tenantId()));

        // Criar Pageable com ordenação
        Pageable pageable = createPageable(request.page(), request.size(), request.sort());

        // Buscar pacientes com filtros (busca e status) ou listagem simples
        Page<Patient> patientsPage = (request.search() != null && !request.search().isBlank())
                || request.active() != null
            ? patientRepository.findByTenantIdWithFilters(
                request.tenantId(),
                request.search() != null && !request.search().isBlank() ? request.search().trim() : null,
                request.active(),
                pageable
            )
            : patientRepository.findByTenantId(request.tenantId(), pageable);

        // Converter para Page<PatientResponse>
        return patientsPage.map(this::toResponse);
    }

    private Pageable createPageable(int page, int size, String sort) {
        Sort sortObj;
        
        if (sort != null && !sort.isEmpty()) {
            // Parse do sort string: "field,direction" ou "field"
            String[] sortParts = sort.split(",");
            String field = sortParts[0].trim();
            String direction = sortParts.length > 1 ? sortParts[1].trim().toUpperCase() : "ASC";
            
            Sort.Direction sortDirection = "DESC".equals(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sortObj = Sort.by(sortDirection, field);
        } else {
            // Ordenação padrão: por firstName ascendente
            sortObj = Sort.by(Sort.Direction.ASC, "firstName");
        }
        
        return PageRequest.of(page, size, sortObj);
    }

    private PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getTenant().getId(),
                patient.getFirstName(),
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

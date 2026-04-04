package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.repository.MedicalRecordRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultListMedicalRecordsUseCase implements ListMedicalRecordsUseCase {

    private final MedicalRecordRepository medicalRecordRepository;
    private final ProfessionalRepository professionalRepository;
    private final TenantContext tenantContext;

    @Override
    public org.springframework.data.domain.Page<MedicalRecordListResponse> execute(
        int page,
        int size,
        String patientName,
        java.time.LocalDateTime dateFrom,
        java.time.LocalDateTime dateTo
    ) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        UUID userId = tenantContext.getUserIdOrNull();

        // Profissional logado: filtrar apenas prontuários dos agendamentos dele; clínica vê todos
        UUID professionalId = (userId != null)
            ? professionalRepository.findByUserIdAndTenantId(userId, tenantId)
                .map(Professional::getId)
                .orElse(null)
            : null;

        String searchName = (patientName != null && !patientName.isBlank()) ? patientName.trim() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return medicalRecordRepository.findPageByTenantAndFilters(
            tenantId,
            professionalId,
            searchName,
            dateFrom,
            dateTo,
            pageable
        );
    }
}

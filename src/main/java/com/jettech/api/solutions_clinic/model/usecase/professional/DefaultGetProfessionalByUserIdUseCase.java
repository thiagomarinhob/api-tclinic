package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetProfessionalByUserIdUseCase implements GetProfessionalByUserIdUseCase {

    private final ProfessionalRepository professionalRepository;
    private final TenantContext tenantContext;

    @Override
    public ProfessionalResponse execute(UUID userId) throws AuthenticationFailedException {
        UUID clinicId = tenantContext.getRequiredClinicId();

        var professional = professionalRepository.findByUserIdAndTenantId(userId, clinicId)
                .orElseThrow(() -> new EntityNotFoundException("Profissional", userId));

        return new ProfessionalResponse(
                professional.getId(),
                professional.getUser().getId(),
                professional.getTenant().getId(),
                professional.getSpecialty(),
                professional.getDocumentType(),
                professional.getDocumentNumber(),
                professional.getDocumentState(),
                professional.getBio(),
                professional.isActive(),
                professional.getCreatedAt(),
                professional.getUpdatedAt()
        );
    }
}

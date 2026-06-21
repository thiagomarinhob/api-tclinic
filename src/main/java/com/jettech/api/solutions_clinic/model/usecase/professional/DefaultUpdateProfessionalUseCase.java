package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultUpdateProfessionalUseCase implements UpdateProfessionalUseCase {

    private final ProfessionalRepository professionalRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ProfessionalResponse execute(UpdateProfessionalRequest request) throws AuthenticationFailedException {
        log.info("Atualizando profissional - professionalId: {}", request.id());
        Professional professional = professionalRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Profissional", request.id()));
        UUID tenantId = tenantContext.getRequiredClinicId();
        if (!professional.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao atualizar profissional {} - tenantId: {}", request.id(), tenantId);
            throw new ForbiddenException();
        }

        log.debug("Atualizando campos do profissional - specialty: {}, documentType: {}", request.specialty(), request.documentType());
        professional.setSpecialty(request.specialty());
        professional.setDocumentType(request.documentType());
        professional.setDocumentNumber(request.documentNumber());
        professional.setDocumentState(request.documentState());
        professional.setBio(request.bio());

        professional = professionalRepository.save(professional);
        log.info("Profissional atualizado - professionalId: {}", professional.getId());

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

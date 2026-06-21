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
public class DefaultUpdateProfessionalActiveUseCase implements UpdateProfessionalActiveUseCase {

    private final ProfessionalRepository professionalRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ProfessionalResponse execute(UpdateProfessionalActiveRequest request) throws AuthenticationFailedException {
        log.info("Atualizando status ativo do profissional - professionalId: {}, active: {}", request.id(), request.active());
        Professional professional = professionalRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Profissional", request.id()));
        UUID tenantId = tenantContext.getRequiredClinicId();
        if (!professional.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao atualizar status do profissional {} - tenantId: {}", request.id(), tenantId);
            throw new ForbiddenException();
        }
        professional.setActive(request.active());
        professional = professionalRepository.save(professional);
        log.info("Status do profissional atualizado - professionalId: {}, active: {}", professional.getId(), professional.isActive());

        // Converter para Response
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

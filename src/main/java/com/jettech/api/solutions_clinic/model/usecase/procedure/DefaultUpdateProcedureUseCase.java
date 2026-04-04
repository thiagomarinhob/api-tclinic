package com.jettech.api.solutions_clinic.model.usecase.procedure;

import com.jettech.api.solutions_clinic.model.entity.Procedure;
import com.jettech.api.solutions_clinic.model.repository.ProcedureRepository;
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
public class DefaultUpdateProcedureUseCase implements UpdateProcedureUseCase {

    private final ProcedureRepository procedureRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ProcedureResponse execute(UpdateProcedureRequest request) throws AuthenticationFailedException {
        Procedure procedure = procedureRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Procedimento", request.id()));
        if (!procedure.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        // Atualizar campos se fornecidos
        if (request.name() != null && !request.name().trim().isEmpty()) {
            procedure.setName(request.name());
        }
        if (request.description() != null) {
            procedure.setDescription(request.description());
        }
        if (request.estimatedDurationMinutes() != null) {
            procedure.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
        }
        if (request.basePrice() != null) {
            procedure.setBasePrice(request.basePrice());
        }
        if (request.professionalCommissionPercent() != null) {
            procedure.setProfessionalCommissionPercent(request.professionalCommissionPercent());
        }

        Procedure savedProcedure = procedureRepository.save(procedure);

        return new ProcedureResponse(
                savedProcedure.getId(),
                savedProcedure.getTenant().getId(),
                savedProcedure.getName(),
                savedProcedure.getDescription(),
                savedProcedure.getEstimatedDurationMinutes(),
                savedProcedure.getBasePrice(),
                savedProcedure.getProfessionalCommissionPercent(),
                savedProcedure.isActive(),
                savedProcedure.getCreatedAt(),
                savedProcedure.getUpdatedAt()
        );
    }
}

package com.jettech.api.solutions_clinic.model.usecase.procedure;

import com.jettech.api.solutions_clinic.model.entity.Procedure;
import com.jettech.api.solutions_clinic.model.repository.ProcedureRepository;
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
public class DefaultUpdateProcedureUseCase implements UpdateProcedureUseCase {

    private final ProcedureRepository procedureRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ProcedureResponse execute(UpdateProcedureRequest request) throws AuthenticationFailedException {
        log.info("Atualizando procedimento - procedureId: {}", request.id());
        Procedure procedure = procedureRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Procedimento", request.id()));
        UUID tenantId = tenantContext.getRequiredClinicId();
        if (!procedure.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao atualizar procedimento {} - tenantId: {}", request.id(), tenantId);
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
        log.info("Procedimento atualizado - procedureId: {}", savedProcedure.getId());

        return ProcedureResponse.from(savedProcedure);
    }
}

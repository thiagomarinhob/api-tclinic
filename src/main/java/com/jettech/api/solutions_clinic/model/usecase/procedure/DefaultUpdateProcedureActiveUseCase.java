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
public class DefaultUpdateProcedureActiveUseCase implements UpdateProcedureActiveUseCase {

    private final ProcedureRepository procedureRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ProcedureResponse execute(UpdateProcedureActiveRequest request) throws AuthenticationFailedException {
        log.info("Atualizando status ativo do procedimento - procedureId: {}, active: {}", request.id(), request.active());
        Procedure procedure = procedureRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Procedimento", request.id()));
        UUID tenantId = tenantContext.getRequiredClinicId();
        if (!procedure.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao atualizar status do procedimento {} - tenantId: {}", request.id(), tenantId);
            throw new ForbiddenException();
        }
        procedure.setActive(request.active());
        procedure = procedureRepository.save(procedure);
        log.info("Status do procedimento atualizado - procedureId: {}, active: {}", procedure.getId(), procedure.isActive());

        return ProcedureResponse.from(procedure);
    }
}

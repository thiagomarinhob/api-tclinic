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
public class DefaultGetProcedureByIdUseCase implements GetProcedureByIdUseCase {

    private final ProcedureRepository procedureRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public ProcedureResponse execute(UUID id) throws AuthenticationFailedException {
        log.info("Buscando procedimento por id: {}", id);
        Procedure procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Procedimento", id));
        UUID tenantId = tenantContext.getRequiredClinicId();
        if (!procedure.getTenant().getId().equals(tenantId)) {
            log.warn("Acesso negado ao procedimento {} - tenantId: {}", id, tenantId);
            throw new ForbiddenException();
        }
        log.info("Procedimento {} encontrado - active: {}", id, procedure.isActive());
        return ProcedureResponse.from(procedure);
    }
}

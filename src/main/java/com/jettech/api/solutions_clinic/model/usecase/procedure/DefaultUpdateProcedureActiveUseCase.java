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
public class DefaultUpdateProcedureActiveUseCase implements UpdateProcedureActiveUseCase {

    private final ProcedureRepository procedureRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public ProcedureResponse execute(UpdateProcedureActiveRequest request) throws AuthenticationFailedException {
        Procedure procedure = procedureRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Procedimento", request.id()));
        if (!procedure.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        procedure.setActive(request.active());
        procedure = procedureRepository.save(procedure);

        return new ProcedureResponse(
                procedure.getId(),
                procedure.getTenant().getId(),
                procedure.getName(),
                procedure.getDescription(),
                procedure.getEstimatedDurationMinutes(),
                procedure.getBasePrice(),
                procedure.getProfessionalCommissionPercent(),
                procedure.isActive(),
                procedure.getCreatedAt(),
                procedure.getUpdatedAt()
        );
    }
}

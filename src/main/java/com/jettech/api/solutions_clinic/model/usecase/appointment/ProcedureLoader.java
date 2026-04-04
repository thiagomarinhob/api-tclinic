package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.InvalidStateException;
import com.jettech.api.solutions_clinic.model.entity.Procedure;
import com.jettech.api.solutions_clinic.model.repository.ProcedureRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ProcedureLoader {

    private final ProcedureRepository procedureRepository;

    public ProcedureLoadResult loadAndValidate(List<UUID> procedureIds, UUID tenantId) {
        List<Procedure> procedures = new ArrayList<>();

        for (UUID procedureId : procedureIds) {
            Procedure procedure = procedureRepository.findByIdAndTenantId(procedureId, tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Procedimento", procedureId));

            if (!procedure.isActive()) {
                throw new InvalidStateException(ApiError.INVALID_STATE_PROCEDURE_INACTIVE);
            }

            procedures.add(procedure);
        }

        int totalDurationMinutes = procedures.stream()
                .mapToInt(Procedure::getEstimatedDurationMinutes)
                .sum();
        BigDecimal totalValueFromProcedures = procedures.stream()
                .map(Procedure::getBasePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProcedureLoadResult(procedures, totalDurationMinutes, totalValueFromProcedures);
    }
}

package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Procedure;

import java.math.BigDecimal;
import java.util.List;

public record ProcedureLoadResult(
        List<Procedure> procedures,
        int totalDurationMinutes,
        BigDecimal totalValueFromProcedures
) {
}

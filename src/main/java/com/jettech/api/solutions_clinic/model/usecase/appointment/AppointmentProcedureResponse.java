package com.jettech.api.solutions_clinic.model.usecase.appointment;

import java.math.BigDecimal;
import java.util.UUID;

public record AppointmentProcedureResponse(
    UUID id,          // ID do Procedure (usado pelo frontend para re-enviar nos updates)
    String name,
    String description,
    BigDecimal value, // finalPrice cobrado
    BigDecimal totalValue
) {
}

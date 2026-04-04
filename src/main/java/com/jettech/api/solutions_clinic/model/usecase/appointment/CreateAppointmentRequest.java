package com.jettech.api.solutions_clinic.model.usecase.appointment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateAppointmentRequest(
    @NotNull(message = "O campo [tenantId] é obrigatório")
    UUID tenantId,
    
    @NotNull(message = "O campo [patientId] é obrigatório")
    UUID patientId,
    
    @NotNull(message = "O campo [professionalId] é obrigatório")
    UUID professionalId,
    
    UUID roomId,
    
    @NotNull(message = "O campo [scheduledAt] é obrigatório")
    LocalDateTime scheduledAt,
    
    @Min(value = 15, message = "O campo [durationMinutes] deve ser no mínimo 15 minutos")
    int durationMinutes,
    
    String observations,
    
    @NotNull(message = "O campo [totalValue] é obrigatório")
    @Min(value = 0, message = "O campo [totalValue] deve ser maior ou igual a zero")
    BigDecimal totalValue,
    
    List<UUID> procedureIds, // Lista de IDs de procedimentos
    
    @NotNull(message = "O campo [createdBy] é obrigatório")
    UUID createdBy,
    
    Boolean forceSchedule // Se true, permite agendamento mesmo com conflito de horário
) {
    public Boolean forceSchedule() {
        return forceSchedule != null && forceSchedule;
    }
}

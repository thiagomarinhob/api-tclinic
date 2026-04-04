package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.PaymentMethod;
import com.jettech.api.solutions_clinic.model.entity.PaymentStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UpdateAppointmentRequest(
    @NotNull(message = "O campo [id] é obrigatório")
    UUID id,

    UUID patientId,

    UUID professionalId,

    UUID roomId,

    LocalDateTime scheduledAt,

    @Min(value = 15, message = "O campo [durationMinutes] deve ser no mínimo 15 minutos")
    Integer durationMinutes,

    String observations,

    List<UUID> procedureIds,

    @Min(value = 0, message = "O campo [totalValue] deve ser maior ou igual a zero")
    BigDecimal totalValue,

    PaymentStatus paymentStatus,

    PaymentMethod paymentMethod,

    Boolean forceSchedule // Se true, permite agendamento mesmo com conflito de horário
) {
    public Boolean forceSchedule() {
        return forceSchedule != null && forceSchedule;
    }
}

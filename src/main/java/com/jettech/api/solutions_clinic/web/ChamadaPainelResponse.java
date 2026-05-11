package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.entity.ChamadaPainel;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChamadaPainelResponse(
        UUID id,
        UUID appointmentId,
        String patientName,
        String roomName,
        short numeroChamada,
        OffsetDateTime horaChamada,
        OffsetDateTime horaAtendido
) {
    public static ChamadaPainelResponse from(ChamadaPainel c) {
        return new ChamadaPainelResponse(
                c.getId(),
                c.getAppointment().getId(),
                c.getAppointment().getPatient().getFirstName(),
                c.getRoom().getName(),
                c.getNumeroChamada(),
                c.getHoraChamada(),
                c.getHoraAtendido()
        );
    }
}

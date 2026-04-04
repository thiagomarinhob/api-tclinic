package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AppointmentResponseMapper {

    public AppointmentResponse toResponse(Appointment appointment) {
        List<AppointmentProcedureResponse> procedures = appointment.getProcedures() == null
                ? List.of()
                : appointment.getProcedures().stream()
                        .map(ap -> new AppointmentProcedureResponse(
                                ap.getProcedure().getId(),
                                ap.getProcedure().getName(),
                                ap.getProcedure().getDescription(),
                                ap.getFinalPrice(),
                                ap.getFinalPrice()
                        ))
                        .collect(Collectors.toList());

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getTenant().getId(),
                appointment.getPatient().getId(),
                appointment.getProfessional().getId(),
                appointment.getRoom() != null ? appointment.getRoom().getId() : null,
                appointment.getScheduledAt(),
                appointment.getDurationMinutes(),
                appointment.getStatus(),
                appointment.getObservations(),
                appointment.getCancelledAt(),
                appointment.getStartedAt(),
                appointment.getFinishedAt(),
                appointment.getDurationActualMinutes(),
                appointment.getTotalValue(),
                appointment.getPaymentMethod(),
                appointment.getPaymentStatus(),
                appointment.getPaidAt(),
                appointment.getCreatedBy().getId(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt(),
                appointment.getVitalSigns() != null ? appointment.getVitalSigns().toString() : null,
                procedures
        );
    }
}

package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.entity.Professional;
import com.jettech.api.solutions_clinic.model.entity.Room;
import com.jettech.api.solutions_clinic.model.entity.User;
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
                toPatientResponse(appointment.getPatient()),
                toProfessionalResponse(appointment.getProfessional()),
                toRoomResponse(appointment.getRoom()),
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
                appointment.getCreatedBy() != null ? appointment.getCreatedBy().getId() : null,
                appointment.getCreatedAt(),
                appointment.getUpdatedAt(),
                appointment.getVitalSigns() != null ? appointment.getVitalSigns().toString() : null,
                procedures,
                appointment.getCancellationReason()
        );
    }

    private AppointmentResponse.AppointmentPatientResponse toPatientResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        return new AppointmentResponse.AppointmentPatientResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getMotherName(),
                patient.getCpf(),
                patient.getBirthDate(),
                patient.getGender() != null ? patient.getGender().name() : null,
                patient.getEmail(),
                patient.getPhone(),
                patient.getWhatsapp(),
                patient.getAddressStreet(),
                patient.getAddressNumber(),
                patient.getAddressComplement(),
                patient.getAddressNeighborhood(),
                patient.getAddressCity(),
                patient.getAddressState(),
                patient.getAddressZipcode(),
                patient.getBloodType() != null ? patient.getBloodType().name() : null,
                patient.getAllergies(),
                patient.getHealthPlan(),
                patient.getGuardianName(),
                patient.getGuardianPhone(),
                patient.getGuardianRelationship(),
                patient.isActive(),
                patient.getCreatedAt()
        );
    }

    private AppointmentResponse.AppointmentProfessionalResponse toProfessionalResponse(Professional professional) {
        if (professional == null) {
            return null;
        }

        return new AppointmentResponse.AppointmentProfessionalResponse(
                professional.getId(),
                toUserResponse(professional.getUser()),
                professional.getSpecialty(),
                professional.getDocumentType(),
                professional.getDocumentNumber(),
                professional.getDocumentState(),
                professional.getBio(),
                professional.isActive()
        );
    }

    private AppointmentResponse.AppointmentUserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        String fullName = (String.join(" ",
                user.getFirstName() != null ? user.getFirstName() : "",
                user.getLastName() != null ? user.getLastName() : ""
        )).trim();

        return new AppointmentResponse.AppointmentUserResponse(
                user.getId(),
                user.getEmail(),
                !fullName.isBlank() ? fullName : user.getEmail(),
                user.getPhone(),
                !user.isBlocked(),
                user.getCreatedAt()
        );
    }

    private AppointmentResponse.AppointmentRoomResponse toRoomResponse(Room room) {
        if (room == null) {
            return null;
        }

        return new AppointmentResponse.AppointmentRoomResponse(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getCapacity(),
                room.isActive(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}

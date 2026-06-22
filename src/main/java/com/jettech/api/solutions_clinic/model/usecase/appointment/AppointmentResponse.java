package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.jettech.api.solutions_clinic.model.entity.DocumentType;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.entity.PaymentMethod;
import com.jettech.api.solutions_clinic.model.entity.PaymentStatus;
import com.jettech.api.solutions_clinic.model.entity.Specialty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AppointmentResponse(
    UUID id,
    UUID tenantId,
    UUID patientId,
    UUID professionalId,
    UUID roomId,
    AppointmentPatientResponse patient,
    AppointmentProfessionalResponse professional,
    AppointmentRoomResponse room,
    LocalDateTime scheduledAt,
    int durationMinutes,
    AppointmentStatus status,
    String observations,
    LocalDateTime cancelledAt,
    LocalDateTime startedAt,
    LocalDateTime finishedAt,
    Integer durationActualMinutes,
    BigDecimal totalValue,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    LocalDateTime paidAt,
    UUID createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    @JsonRawValue String vitalSigns,
    List<AppointmentProcedureResponse> procedures,
    String cancellationReason
) {
    public record AppointmentPatientResponse(
        UUID id,
        String fullName,
        String motherName,
        String cpf,
        String birthDate,
        String gender,
        String email,
        String phone,
        String whatsapp,
        String addressStreet,
        String addressNumber,
        String addressComplement,
        String addressNeighborhood,
        String addressCity,
        String addressState,
        String addressZipcode,
        String bloodType,
        String allergies,
        String healthPlan,
        String guardianName,
        String guardianPhone,
        String guardianRelationship,
        boolean isActive,
        LocalDateTime createdAt
    ) {
    }

    public record AppointmentProfessionalResponse(
        UUID id,
        AppointmentUserResponse user,
        Specialty specialty,
        DocumentType documentType,
        String documentNumber,
        String documentState,
        String bio,
        boolean isActive
    ) {
    }

    public record AppointmentUserResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        boolean isActive,
        LocalDateTime createdAt
    ) {
    }

    public record AppointmentRoomResponse(
        UUID id,
        String name,
        String description,
        Integer capacity,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }
}

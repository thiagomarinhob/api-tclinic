package com.jettech.api.solutions_clinic.model.usecase.patient;

import com.jettech.api.solutions_clinic.model.entity.BloodType;
import com.jettech.api.solutions_clinic.model.entity.Gender;

import java.time.LocalDateTime;
import java.util.UUID;

public record PatientResponse(
    UUID id,
    UUID tenantId,
    String firstName,
    String motherName,
    String cpf,
    String birthDate,
    Gender gender,
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
    BloodType bloodType,
    String allergies,
    String healthPlan,
    String guardianName,
    String guardianPhone,
    String guardianRelationship,
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}


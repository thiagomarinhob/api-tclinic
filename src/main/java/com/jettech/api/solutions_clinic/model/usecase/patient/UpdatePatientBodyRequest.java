package com.jettech.api.solutions_clinic.model.usecase.patient;

import com.jettech.api.solutions_clinic.model.entity.BloodType;
import com.jettech.api.solutions_clinic.model.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePatientBodyRequest(
    @NotBlank(message = "O campo [firstName] é obrigatório")
    @Size(min = 2, max = 100, message = "O campo [firstName] deve ter entre 2 e 100 caracteres")
    String firstName,

    @Pattern(regexp = "\\d{11}", message = "O campo [cpf] deve conter exatamente 11 dígitos")
    String cpf,

    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "O campo [birthDate] deve ser uma data válida no formato DD/MM/YYYY")
    String birthDate,

    Gender gender,

    @Email(message = "O campo [email] deve ser um email válido")
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
    String guardianRelationship
) {
}

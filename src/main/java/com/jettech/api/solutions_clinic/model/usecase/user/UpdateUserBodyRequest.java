package com.jettech.api.solutions_clinic.model.usecase.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateUserBodyRequest(
    String firstName,
    String lastName,
    String phone,
    @Pattern(regexp = "\\d{11}", message = "O campo [cpf] deve conter exatamente 11 dígitos")
    String cpf,
    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "O campo [birthDate] deve ser uma data válida no formato DD/MM/YYYY")
    String birthDate,
    @Email(message = "O campo [email] deve ser um email válido")
    String email
) {
}

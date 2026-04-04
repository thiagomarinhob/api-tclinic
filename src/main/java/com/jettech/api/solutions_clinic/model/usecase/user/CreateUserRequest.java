package com.jettech.api.solutions_clinic.model.usecase.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record CreateUserRequest(
    @NotBlank(message = "O campo [firstName] é obrigatório")
    String firstName,
    
    @NotBlank(message = "O campo [lastName] é obrigatório")
    String lastName,
    
    @NotBlank(message = "O campo [email] é obrigatório")
    @Email(message = "O campo [email] deve ser um email válido")
    String email,
    
    @NotBlank(message = "O campo [password] é obrigatório")
    String password,
    
    String phone,
    
    @Pattern(regexp = "\\d{11}", message = "O campo [cpf] deve conter exatamente 11 dígitos")
    String cpf,
    
    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "O campo [birthDate] deve ser uma data válida no formato DD/MM/YYYY")
    String birthDate,
    
    UUID tenantId
) {
}

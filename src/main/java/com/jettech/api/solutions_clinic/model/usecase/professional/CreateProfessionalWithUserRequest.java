package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.model.entity.DocumentType;
import com.jettech.api.solutions_clinic.model.entity.Specialty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProfessionalWithUserRequest(
    // User fields
    @NotBlank(message = "O campo [firstName] é obrigatório")
    @Size(min = 2, max = 50, message = "O campo [firstName] deve ter entre 2 e 50 caracteres")
    String firstName,
    
    @NotBlank(message = "O campo [lastName] é obrigatório")
    @Size(min = 2, max = 50, message = "O campo [lastName] deve ter entre 2 e 50 caracteres")
    String lastName,
    
    @NotBlank(message = "O campo [email] é obrigatório")
    @Email(message = "O campo [email] deve ser um email válido")
    String email,
    
    @NotBlank(message = "O campo [password] é obrigatório")
    @Size(min = 8, message = "O campo [password] deve ter no mínimo 8 caracteres")
    String password,
    
    @Size(max = 20, message = "O campo [phone] deve ter no máximo 20 caracteres")
    String phone,
    
    @Pattern(regexp = "\\d{11}", message = "O campo [cpf] deve conter exatamente 11 dígitos")
    String cpf,
    
    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "O campo [birthDate] deve ser uma data válida no formato DD/MM/YYYY")
    String birthDate,
    
    // Professional fields
    @NotNull(message = "O campo [specialty] é obrigatório")
    Specialty specialty,
    
    @NotNull(message = "O campo [documentType] é obrigatório")
    DocumentType documentType,
    
    @NotBlank(message = "O campo [documentNumber] é obrigatório")
    String documentNumber,
    
    @Size(max = 2, message = "O campo [documentState] deve ter no máximo 2 caracteres")
    String documentState,
    
    String bio
) {
}

package com.jettech.api.solutions_clinic.model.usecase.signup;

import com.jettech.api.solutions_clinic.model.entity.PlanType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpSoloRequest(
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
    
    @NotBlank(message = "O campo [birthDate] é obrigatório")
    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "O campo [birthDate] deve ser uma data válida no formato DD/MM/YYYY")
    String birthDate,
    
    // Tenant fields
    @NotBlank(message = "O campo [name] é obrigatório")
    @Size(min = 2, max = 100, message = "O campo [name] deve ter entre 2 e 100 caracteres")
    String name,
    
    @NotBlank(message = "O campo [cpf] é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O campo [cpf] deve conter exatamente 11 dígitos")
    String cpf,
    
    PlanType planType,
    
    @Size(max = 200, message = "O campo [address] deve ter no máximo 200 caracteres")
    String address,
    
    @Size(max = 20, message = "O campo [phone] deve ter no máximo 20 caracteres")
    String phone,
    
    @NotBlank(message = "O campo [subdomain] é obrigatório")
    @Size(min = 3, max = 64, message = "O campo [subdomain] deve ter entre 3 e 64 caracteres")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "O campo [subdomain] deve conter apenas letras minúsculas, números e hífens")
    String subdomain
) {
}


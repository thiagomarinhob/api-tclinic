package com.jettech.api.solutions_clinic.model.usecase.room;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateRoomRequest(
    @NotNull(message = "O campo [tenantId] é obrigatório")
    UUID tenantId,
    
    @NotBlank(message = "O campo [name] é obrigatório")
    @Size(min = 1, max = 255, message = "O campo [name] deve ter entre 1 e 255 caracteres")
    String name,
    
    String description,
    
    @NotNull(message = "O campo [capacity] é obrigatório")
    @Min(value = 1, message = "O campo [capacity] deve ser maior que zero")
    Integer capacity
) {
}


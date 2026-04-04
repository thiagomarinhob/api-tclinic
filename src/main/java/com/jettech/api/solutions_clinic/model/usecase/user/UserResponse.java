package com.jettech.api.solutions_clinic.model.usecase.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String firstName,
    String lastName,
    String email,
    String phone,
    String cpf,
    String birthDate,
    boolean blocked,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

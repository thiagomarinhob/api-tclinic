package com.jettech.api.solutions_clinic.model.usecase.admin;

import java.util.UUID;

public record AdminOwnerResponse(
        UUID userId,
        String firstName,
        String lastName,
        String email,
        String phone
) {}

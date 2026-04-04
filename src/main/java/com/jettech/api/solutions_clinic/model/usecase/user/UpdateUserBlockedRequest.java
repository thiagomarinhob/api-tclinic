package com.jettech.api.solutions_clinic.model.usecase.user;

import java.util.UUID;

public record UpdateUserBlockedRequest(
    UUID id,
    boolean blocked
) {
}

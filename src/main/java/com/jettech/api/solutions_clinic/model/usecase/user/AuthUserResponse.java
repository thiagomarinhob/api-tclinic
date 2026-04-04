package com.jettech.api.solutions_clinic.model.usecase.user;

public record AuthUserResponse(String access_token, Long expires_in) {
}

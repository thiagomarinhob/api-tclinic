package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import java.util.UUID;

public interface GetUserByIdUseCase extends UseCase<UUID, UserDetailResponse> {
}


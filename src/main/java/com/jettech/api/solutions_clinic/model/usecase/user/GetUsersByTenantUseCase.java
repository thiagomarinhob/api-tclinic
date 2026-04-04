package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;
import org.springframework.data.domain.Page;

public interface GetUsersByTenantUseCase extends UseCase<GetUsersByTenantRequest, Page<UserResponse>> {
}

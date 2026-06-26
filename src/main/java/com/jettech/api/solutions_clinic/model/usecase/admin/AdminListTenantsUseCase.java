package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;
import org.springframework.data.domain.Page;

public interface AdminListTenantsUseCase extends UseCase<AdminListTenantsRequest, Page<AdminTenantListItemResponse>> {
}

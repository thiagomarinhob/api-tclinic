package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import org.springframework.data.domain.Page;

public interface GetLabExamTypesByTenantUseCase {
    Page<LabExamTypeResponse> execute(GetLabExamTypesByTenantRequest request) throws AuthenticationFailedException;
}

package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

public interface GetLabDashboardUseCase {
    LabDashboardResponse execute() throws AuthenticationFailedException;
}

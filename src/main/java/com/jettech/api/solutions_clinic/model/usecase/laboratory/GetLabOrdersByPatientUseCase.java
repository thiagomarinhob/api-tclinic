package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.List;
import java.util.UUID;

public interface GetLabOrdersByPatientUseCase {
    List<LabOrderResponse> execute(UUID patientId) throws AuthenticationFailedException;
}

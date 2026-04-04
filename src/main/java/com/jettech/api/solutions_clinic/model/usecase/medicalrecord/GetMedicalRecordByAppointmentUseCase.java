package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.Optional;
import java.util.UUID;

public interface GetMedicalRecordByAppointmentUseCase {
    Optional<MedicalRecordResponse> execute(UUID appointmentId) throws AuthenticationFailedException;
}

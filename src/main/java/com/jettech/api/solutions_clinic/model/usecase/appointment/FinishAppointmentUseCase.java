package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.UUID;

public interface FinishAppointmentUseCase {

    AppointmentResponse execute(UUID appointmentId, FinishAppointmentRequest request) throws AuthenticationFailedException;
}

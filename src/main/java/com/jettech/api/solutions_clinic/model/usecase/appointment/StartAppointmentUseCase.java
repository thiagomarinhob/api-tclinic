package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.UUID;

public interface StartAppointmentUseCase {

    AppointmentResponse execute(UUID appointmentId) throws AuthenticationFailedException;
}

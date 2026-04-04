package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import java.util.UUID;

public interface GetAppointmentByIdUseCase extends UseCase<UUID, AppointmentResponse> {
}

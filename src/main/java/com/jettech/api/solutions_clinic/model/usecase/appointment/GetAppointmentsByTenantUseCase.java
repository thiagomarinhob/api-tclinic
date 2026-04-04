package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import java.util.List;

public interface GetAppointmentsByTenantUseCase extends UseCase<GetAppointmentsByTenantRequest, List<AppointmentResponse>> {
}

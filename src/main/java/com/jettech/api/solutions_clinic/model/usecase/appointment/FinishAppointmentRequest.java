package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.PaymentMethod;
import com.jettech.api.solutions_clinic.model.entity.PaymentStatus;

public record FinishAppointmentRequest(
    String observations,
    PaymentStatus paymentStatus,
    PaymentMethod paymentMethod
) {}

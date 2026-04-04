package com.jettech.api.solutions_clinic.model.usecase.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateExamRequest(
    @NotNull(message = "O campo [patientId] é obrigatório")
    UUID patientId,

    UUID appointmentId,

    @NotBlank(message = "O campo [name] é obrigatório")
    @Size(min = 1, max = 255)
    String name,

    @Size(max = 5000)
    String clinicalIndication
) {}

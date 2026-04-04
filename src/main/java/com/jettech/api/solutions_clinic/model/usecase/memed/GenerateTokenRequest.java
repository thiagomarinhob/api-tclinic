package com.jettech.api.solutions_clinic.model.usecase.memed;

import lombok.Data;
import java.util.UUID;

@Data
public class GenerateTokenRequest {
    private String professionalId;
    private UUID appointmentId;
}

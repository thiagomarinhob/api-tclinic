package com.jettech.api.solutions_clinic.model.usecase.memed;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MedicalDocumentResponse {
    private UUID id;
    private UUID appointmentId;
    private String documentUrl;
    private String documentType;
    private String source;
    private LocalDateTime createdAt;
}
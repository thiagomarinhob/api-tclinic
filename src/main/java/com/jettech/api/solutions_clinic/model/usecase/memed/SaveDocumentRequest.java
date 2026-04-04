package com.jettech.api.solutions_clinic.model.usecase.memed;

import lombok.Data;
import java.util.UUID;

@Data
public class SaveDocumentRequest {
    private UUID appointmentId;
    private String documentUrl;
    private String documentType;
}

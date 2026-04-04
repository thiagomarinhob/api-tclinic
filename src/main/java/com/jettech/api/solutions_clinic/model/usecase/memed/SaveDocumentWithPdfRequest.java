package com.jettech.api.solutions_clinic.model.usecase.memed;

import lombok.Data;
import java.util.UUID;

@Data
public class SaveDocumentWithPdfRequest {
    private UUID appointmentId;
    private String prescriptionId;
    private String userToken;
    private String documentType;
}

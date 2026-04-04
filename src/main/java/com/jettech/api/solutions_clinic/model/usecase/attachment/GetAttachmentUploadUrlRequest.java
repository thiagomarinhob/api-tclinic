package com.jettech.api.solutions_clinic.model.usecase.attachment;

import java.util.UUID;

public record GetAttachmentUploadUrlRequest(UUID appointmentId, String fileName) {

    public String effectiveFileName() {
        return (fileName == null || fileName.isBlank()) ? "anexo.pdf" : fileName;
    }
}

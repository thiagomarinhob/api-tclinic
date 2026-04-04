package com.jettech.api.solutions_clinic.model.usecase.exam;

import java.util.UUID;

/**
 * Parâmetros para gerar URL de upload. fileName é opcional (default "resultado.pdf").
 */
public record GetPresignedUploadUrlRequest(
    UUID examId,
    String fileName
) {
    public String effectiveFileName() {
        return (fileName != null && !fileName.isBlank()) ? fileName : "resultado.pdf";
    }
}

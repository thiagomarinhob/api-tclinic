package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.UUID;

/**
 * Caso de uso: exportar prontuário como PDF.
 */
public interface ExportMedicalRecordPdfUseCase {

    /**
     * Gera o PDF do prontuário. O tenant é obtido do contexto de autenticação.
     *
     * @param id ID do prontuário
     * @return bytes do PDF
     * @throws com.jettech.api.solutions_clinic.exception.EntityNotFoundException se o prontuário não existir ou não pertencer ao tenant
     */
    byte[] execute(UUID id) throws AuthenticationFailedException;
}

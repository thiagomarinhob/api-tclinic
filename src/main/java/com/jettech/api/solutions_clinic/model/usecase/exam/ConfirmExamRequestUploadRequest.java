package com.jettech.api.solutions_clinic.model.usecase.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Corpo da requisição após o frontend ter feito upload da solicitação/prescrição médica no R2.
 * O backend atualiza o exame com a chave do objeto sem alterar o status.
 */
public record ConfirmExamRequestUploadRequest(
    @NotNull(message = "O campo [examId] é obrigatório")
    UUID examId,

    @NotBlank(message = "O campo [objectKey] é obrigatório")
    String objectKey
) {}

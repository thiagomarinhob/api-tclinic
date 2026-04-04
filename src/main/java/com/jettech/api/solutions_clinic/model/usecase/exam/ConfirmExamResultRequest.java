package com.jettech.api.solutions_clinic.model.usecase.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Corpo da requisição após o frontend ter feito upload do arquivo no R2.
 * O backend atualiza o exame com a chave do objeto e marca como COMPLETED.
 */
public record ConfirmExamResultRequest(
    @NotNull(message = "O campo [examId] é obrigatório")
    UUID examId,

    @NotBlank(message = "O campo [objectKey] é obrigatório")
    String objectKey
) {}

package com.jettech.api.solutions_clinic.model.usecase.document;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record GenerateExamesPdfRequest(
    @NotEmpty List<String> exames,
    String indicacaoClinica
) {}

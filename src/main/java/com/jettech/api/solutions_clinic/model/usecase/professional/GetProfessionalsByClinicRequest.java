package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.model.entity.DocumentType;

import java.util.UUID;

public record GetProfessionalsByClinicRequest(
    UUID clinicId,
    int page,
    int size,
    String sort, // Ex: "user.fullName,asc" ou "specialty,desc"
    String search, // Busca textual em nome, email, documento, especialidade
    Boolean active, // null = todos, true = ativos, false = inativos
    DocumentType documentType // null = todos os tipos
) {
}

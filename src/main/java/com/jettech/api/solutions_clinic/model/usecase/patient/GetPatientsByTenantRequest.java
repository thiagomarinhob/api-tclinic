package com.jettech.api.solutions_clinic.model.usecase.patient;

import java.util.UUID;

public record GetPatientsByTenantRequest(
    UUID tenantId,
    int page,
    int size,
    String sort, // Ex: "firstName,asc" ou "createdAt,desc"
    String search, // Busca em nome, CPF, telefone, email
    Boolean active  // null = todos, true = ativos, false = inativos
) {
}

package com.jettech.api.solutions_clinic.model.usecase.user;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request para troca de clínica (tenant) na sessão.
 * O usuário deve ter vínculo (UserTenantRole) com o tenant informado.
 */
public record SwitchTenantRequest(@NotNull(message = "O ID da clínica é obrigatório") UUID tenantId) {
}

package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

/**
 * Troca o contexto de clínica (tenant) da sessão do usuário.
 * Valida se o usuário tem vínculo com o tenant e retorna um novo JWT com o clinicId escolhido.
 */
public interface SwitchTenantUseCase extends UseCase<SwitchTenantRequest, AuthUserResponse> {
}

package com.jettech.api.solutions_clinic.exception;

/**
 * Exceção lançada quando uma entidade não é encontrada (ex.: clínica, paciente, profissional, sala).
 * Preferir construtores com ApiError para mensagens padronizadas.
 */
public class EntityNotFoundException extends RuntimeException implements HasApiError {

    private final ApiError apiError;
    private final Object[] args;

    public EntityNotFoundException(String message) {
        super(message);
        this.apiError = null;
        this.args = null;
    }

    public EntityNotFoundException(String entityType, Object id) {
        super(entityType + " não encontrado(a) com ID: " + id);
        this.apiError = ApiError.ENTITY_NOT_FOUND;
        this.args = new Object[]{entityType, id};
    }

    public EntityNotFoundException(ApiError apiError, Object... args) {
        super(apiError.formatMessage(args));
        this.apiError = apiError;
        this.args = args == null ? new Object[0] : args;
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.apiError = null;
        this.args = null;
    }

    @Override
    public ApiError getApiError() {
        return apiError;
    }

    @Override
    public Object[] getArgs() {
        return args != null ? args : new Object[0];
    }
}

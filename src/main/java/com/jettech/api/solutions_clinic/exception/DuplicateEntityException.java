package com.jettech.api.solutions_clinic.exception;

/**
 * Exceção lançada quando já existe uma entidade com os mesmos dados (ex.: paciente com CPF, profissional duplicado).
 * Preferir construtores com ApiError para mensagens padronizadas.
 */
public class DuplicateEntityException extends RuntimeException implements HasApiError {

    private final ApiError apiError;
    private final Object[] args;

    public DuplicateEntityException(String message) {
        super(message);
        this.apiError = null;
        this.args = null;
    }

    public DuplicateEntityException(ApiError apiError, Object... args) {
        super(apiError.formatMessage(args));
        this.apiError = apiError;
        this.args = args == null ? new Object[0] : args;
    }

    public DuplicateEntityException(String message, Throwable cause) {
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

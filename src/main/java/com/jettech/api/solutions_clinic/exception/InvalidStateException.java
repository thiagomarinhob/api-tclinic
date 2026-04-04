package com.jettech.api.solutions_clinic.exception;

/**
 * Exceção lançada quando uma operação não é permitida no estado atual da entidade
 * (ex.: atualizar agendamento cancelado, procedimento inativo).
 * Preferir construtor com ApiError para mensagens padronizadas.
 */
public class InvalidStateException extends RuntimeException implements HasApiError {

    private final ApiError apiError;
    private final Object[] args;

    public InvalidStateException(String message) {
        super(message);
        this.apiError = null;
        this.args = null;
    }

    public InvalidStateException(ApiError apiError, Object... args) {
        super(apiError.formatMessage(args));
        this.apiError = apiError;
        this.args = args == null ? new Object[0] : args;
    }

    public InvalidStateException(String message, Throwable cause) {
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

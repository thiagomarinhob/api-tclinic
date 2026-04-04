package com.jettech.api.solutions_clinic.exception;

/**
 * Exceção lançada quando a validação de agenda falha (horário fora do expediente,
 * intervalo de almoço, duração inválida, etc.).
 * Preferir construtor com ApiError para mensagens padronizadas.
 */
public class ScheduleValidationException extends RuntimeException implements HasApiError {

    private final ApiError apiError;
    private final Object[] args;

    public ScheduleValidationException(String message) {
        super(message);
        this.apiError = null;
        this.args = null;
    }

    public ScheduleValidationException(ApiError apiError, Object... args) {
        super(apiError.formatMessage(args));
        this.apiError = apiError;
        this.args = args == null ? new Object[0] : args;
    }

    public ScheduleValidationException(String message, Throwable cause) {
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

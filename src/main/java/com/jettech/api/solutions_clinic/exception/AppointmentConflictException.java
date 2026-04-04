package com.jettech.api.solutions_clinic.exception;

/**
 * Exceção lançada quando há conflito de horário em agendamento (profissional ou sala).
 * Preferir construtor com ApiError para mensagem padronizada; detalhe pode ser passado como arg.
 */
public class AppointmentConflictException extends RuntimeException implements HasApiError {

    private final ApiError apiError;
    private final Object[] args;

    public AppointmentConflictException(String message) {
        super(message);
        this.apiError = ApiError.APPOINTMENT_CONFLICT;
        this.args = new Object[0];
    }

    public AppointmentConflictException(ApiError apiError, Object... args) {
        super(apiError.formatMessage(args));
        this.apiError = apiError;
        this.args = args == null ? new Object[0] : args;
    }

    public AppointmentConflictException(String message, Throwable cause) {
        super(message, cause);
        this.apiError = ApiError.APPOINTMENT_CONFLICT;
        this.args = new Object[0];
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

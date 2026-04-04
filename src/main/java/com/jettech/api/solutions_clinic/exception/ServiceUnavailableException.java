package com.jettech.api.solutions_clinic.exception;

/**
 * Exceção para recurso/serviço indisponível (ex.: R2 não configurado).
 * O status HTTP é obtido de {@link ApiError#getStatus()}.
 */
public class ServiceUnavailableException extends RuntimeException implements HasApiError {

    private final ApiError apiError;
    private final Object[] args;

    public ServiceUnavailableException(ApiError apiError, Object... args) {
        super(apiError != null ? apiError.formatMessage(args) : "Serviço indisponível.");
        this.apiError = apiError != null ? apiError : ApiError.R2_NOT_CONFIGURED;
        this.args = args == null ? new Object[0] : args;
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

package com.jettech.api.solutions_clinic.exception;

/**
 * Exceção lançada quando o usuário autenticado não tem permissão para acessar o recurso
 * (ex.: recurso de outra clínica/tenant).
 */
public class ForbiddenException extends RuntimeException implements HasApiError {

    private final ApiError apiError;
    private final Object[] args;

    public ForbiddenException() {
        super(ApiError.ACCESS_DENIED.getDefaultMessage());
        this.apiError = ApiError.ACCESS_DENIED;
        this.args = new Object[0];
    }

    public ForbiddenException(ApiError apiError, Object... args) {
        super(apiError != null ? apiError.formatMessage(args) : ApiError.ACCESS_DENIED.getDefaultMessage());
        this.apiError = apiError != null ? apiError : ApiError.ACCESS_DENIED;
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

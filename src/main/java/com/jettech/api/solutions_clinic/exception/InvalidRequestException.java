package com.jettech.api.solutions_clinic.exception;

/**
 * Exceção lançada quando a requisição ou parâmetros violam regras de negócio
 * (ex.: papel inválido, tipo de categoria incompatível, plano não suportado).
 * Preferir construtores com ApiError para mensagens padronizadas.
 */
public class InvalidRequestException extends RuntimeException implements HasApiError {

    private final ApiError apiError;
    private final Object[] args;

    public InvalidRequestException(String message) {
        super(message);
        this.apiError = null;
        this.args = null;
    }

    public InvalidRequestException(ApiError apiError, Object... args) {
        super(apiError.formatMessage(args));
        this.apiError = apiError;
        this.args = args == null ? new Object[0] : args;
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
        this.apiError = null;
        this.args = null;
    }

    public InvalidRequestException(ApiError apiError, Throwable cause, Object... args) {
        super(apiError.formatMessage(args), cause);
        this.apiError = apiError;
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

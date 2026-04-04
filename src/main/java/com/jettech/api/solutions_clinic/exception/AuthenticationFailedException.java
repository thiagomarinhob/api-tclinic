package com.jettech.api.solutions_clinic.exception;

import java.security.GeneralSecurityException;

/**
 * Exceção de falha de autenticação/autorização.
 * Estende GeneralSecurityException em vez de javax.naming.AuthenticationException (JNDI).
 * Preferir construtor com ApiError para mensagem padronizada.
 */
public class AuthenticationFailedException extends GeneralSecurityException implements HasApiError {

    private final ApiError apiError;
    private final Object[] args;

    public AuthenticationFailedException() {
        super();
        this.apiError = ApiError.AUTHENTICATION_FAILED;
        this.args = new Object[0];
    }

    public AuthenticationFailedException(String message) {
        super(message);
        this.apiError = null;
        this.args = null;
    }

    public AuthenticationFailedException(ApiError apiError, Object... args) {
        super(apiError.formatMessage(args));
        this.apiError = apiError;
        this.args = args == null ? new Object[0] : args;
    }

    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
        this.apiError = null;
        this.args = null;
    }

    public AuthenticationFailedException(Throwable cause) {
        super(cause);
        this.apiError = ApiError.AUTHENTICATION_FAILED;
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

package com.jettech.api.solutions_clinic.exception;

/**
 * Exceções que carregam um código de erro padronizado (ApiError) e argumentos opcionais
 * para formatação da mensagem. O handler usa esses dados em vez de getMessage().
 */
public interface HasApiError {

    ApiError getApiError();

    /** Argumentos para formatação da mensagem (pode ser vazio). */
    Object[] getArgs();
}

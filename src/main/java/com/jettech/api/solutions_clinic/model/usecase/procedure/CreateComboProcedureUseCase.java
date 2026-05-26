package com.jettech.api.solutions_clinic.model.usecase.procedure;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

public interface CreateComboProcedureUseCase {
    ProcedureResponse execute(CreateComboProcedureRequest request) throws AuthenticationFailedException;
}

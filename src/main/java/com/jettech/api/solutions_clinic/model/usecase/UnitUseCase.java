package com.jettech.api.solutions_clinic.model.usecase;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

public interface UnitUseCase<IN> {

    void execute(IN in) throws AuthenticationFailedException;

}

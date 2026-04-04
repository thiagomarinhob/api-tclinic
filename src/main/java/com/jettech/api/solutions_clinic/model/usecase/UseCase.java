package com.jettech.api.solutions_clinic.model.usecase;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

public interface UseCase<IN, OUT> {

    OUT execute(IN in) throws AuthenticationFailedException;

}

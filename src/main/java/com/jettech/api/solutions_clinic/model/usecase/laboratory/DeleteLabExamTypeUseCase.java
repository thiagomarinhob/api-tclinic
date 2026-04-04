package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.UUID;

public interface DeleteLabExamTypeUseCase {
    void execute(UUID id) throws AuthenticationFailedException;
}

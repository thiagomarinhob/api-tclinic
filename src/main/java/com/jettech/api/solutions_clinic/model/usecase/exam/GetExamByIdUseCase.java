package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import java.util.UUID;

public interface GetExamByIdUseCase extends UseCase<UUID, ExamResponse> {}

package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.usecase.UseCase;

public interface ConfirmExamResultUploadUseCase extends UseCase<ConfirmExamResultRequest, ExamResponse> {}

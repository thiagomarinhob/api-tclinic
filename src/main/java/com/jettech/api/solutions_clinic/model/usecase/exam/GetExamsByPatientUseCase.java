package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.usecase.UseCase;
import org.springframework.data.domain.Page;

public interface GetExamsByPatientUseCase extends UseCase<GetExamsByPatientRequest, Page<ExamResponse>> {}

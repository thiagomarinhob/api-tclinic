package com.jettech.api.solutions_clinic.model.usecase.exam;

import java.util.List;

public interface GetExamTypesUseCase {
    List<ExamTypeResponse> execute();
}

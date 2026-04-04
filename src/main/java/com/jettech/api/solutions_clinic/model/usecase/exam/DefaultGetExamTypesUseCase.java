package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.model.repository.ExamTypeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetExamTypesUseCase implements GetExamTypesUseCase {

    private final ExamTypeRepository examTypeRepository;

    @Override
    public List<ExamTypeResponse> execute() {
        return examTypeRepository.findByActiveTrueOrderByCategoryAscDisplayOrderAsc()
                .stream()
                .map(t -> new ExamTypeResponse(t.getId(), t.getCategory(), t.getName()))
                .toList();
    }
}

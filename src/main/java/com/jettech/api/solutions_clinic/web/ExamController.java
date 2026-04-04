package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.exam.ConfirmExamResultRequest;
import com.jettech.api.solutions_clinic.model.usecase.exam.ConfirmExamRequestUploadRequest;
import com.jettech.api.solutions_clinic.model.usecase.exam.ConfirmExamRequestUploadUseCase;
import com.jettech.api.solutions_clinic.model.usecase.exam.CreateExamRequest;
import com.jettech.api.solutions_clinic.model.usecase.exam.ExamResponse;
import com.jettech.api.solutions_clinic.model.entity.ExamStatus;
import com.jettech.api.solutions_clinic.model.usecase.exam.GetExamsByPatientRequest;
import com.jettech.api.solutions_clinic.model.usecase.exam.GetExamsByTenantRequest;
import com.jettech.api.solutions_clinic.model.usecase.exam.ExamListResponse;
import com.jettech.api.solutions_clinic.model.usecase.exam.ExamResultViewUrlResponse;
import com.jettech.api.solutions_clinic.model.usecase.exam.GetPresignedUploadUrlRequest;
import com.jettech.api.solutions_clinic.model.usecase.exam.PresignedUploadUrlResponse;
import com.jettech.api.solutions_clinic.model.usecase.exam.ConfirmExamResultUploadUseCase;
import com.jettech.api.solutions_clinic.model.usecase.exam.CreateExamUseCase;
import com.jettech.api.solutions_clinic.model.usecase.exam.GetExamByIdUseCase;
import com.jettech.api.solutions_clinic.model.usecase.exam.GetExamsByPatientUseCase;
import com.jettech.api.solutions_clinic.model.usecase.exam.GetExamResultViewUrlUseCase;
import com.jettech.api.solutions_clinic.model.usecase.exam.GetExamRequestUploadUrlUseCase;
import com.jettech.api.solutions_clinic.model.usecase.exam.GetExamRequestViewUrlUseCase;
import com.jettech.api.solutions_clinic.model.usecase.exam.GetExamsByTenantUseCase;
import com.jettech.api.solutions_clinic.model.usecase.exam.GetExamTypesUseCase;
import com.jettech.api.solutions_clinic.model.usecase.exam.ExamTypeResponse;
import com.jettech.api.solutions_clinic.model.usecase.exam.GetPresignedUploadUrlUseCase;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ExamController implements ExamAPI {

    private final GetExamTypesUseCase getExamTypesUseCase;
    private final CreateExamUseCase createExamUseCase;
    private final GetExamByIdUseCase getExamByIdUseCase;
    private final GetExamsByPatientUseCase getExamsByPatientUseCase;
    private final GetExamsByTenantUseCase getExamsByTenantUseCase;
    private final GetPresignedUploadUrlUseCase getPresignedUploadUrlUseCase;
    private final ConfirmExamResultUploadUseCase confirmExamResultUploadUseCase;
    private final GetExamResultViewUrlUseCase getExamResultViewUrlUseCase;
    private final GetExamRequestUploadUrlUseCase getExamRequestUploadUrlUseCase;
    private final ConfirmExamRequestUploadUseCase confirmExamRequestUploadUseCase;
    private final GetExamRequestViewUrlUseCase getExamRequestViewUrlUseCase;

    @Override
    public List<ExamTypeResponse> getExamTypes() {
        return getExamTypesUseCase.execute();
    }

    @Override
    public Page<ExamListResponse> getExamsByTenant(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) String status) throws AuthenticationFailedException {
        ExamStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = ExamStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return getExamsByTenantUseCase.execute(new GetExamsByTenantRequest(page, size, patientId, statusEnum));
    }

    @Override
    public ExamResponse createExam(@Valid @RequestBody CreateExamRequest request) throws AuthenticationFailedException {
        return createExamUseCase.execute(request);
    }

    @Override
    public ExamResponse getExamById(@PathVariable UUID id) throws AuthenticationFailedException {
        return getExamByIdUseCase.execute(id);
    }

    @Override
    public Page<ExamResponse> getExamsByPatient(
            @RequestParam UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws AuthenticationFailedException {
        return getExamsByPatientUseCase.execute(new GetExamsByPatientRequest(patientId, page, size));
    }

    @Override
    public PresignedUploadUrlResponse getPresignedUploadUrl(
            @PathVariable UUID examId,
            @RequestParam(required = false) String fileName) throws AuthenticationFailedException {
        return getPresignedUploadUrlUseCase.execute(new GetPresignedUploadUrlRequest(examId, fileName));
    }

    @Override
    public ExamResponse confirmExamResultUpload(@Valid @RequestBody ConfirmExamResultRequest request) throws AuthenticationFailedException {
        return confirmExamResultUploadUseCase.execute(request);
    }

    @Override
    public ExamResultViewUrlResponse getExamResultViewUrl(@PathVariable UUID id) throws AuthenticationFailedException {
        return getExamResultViewUrlUseCase.execute(id);
    }

    @Override
    public PresignedUploadUrlResponse getPresignedRequestUploadUrl(
            @PathVariable UUID examId,
            @RequestParam(required = false) String fileName) throws AuthenticationFailedException {
        return getExamRequestUploadUrlUseCase.execute(new GetPresignedUploadUrlRequest(examId, fileName));
    }

    @Override
    public ExamResponse confirmExamRequestUpload(@Valid @RequestBody ConfirmExamRequestUploadRequest request) throws AuthenticationFailedException {
        return confirmExamRequestUploadUseCase.execute(request);
    }

    @Override
    public ExamResultViewUrlResponse getExamRequestViewUrl(@PathVariable UUID id) throws AuthenticationFailedException {
        return getExamRequestViewUrlUseCase.execute(id);
    }
}

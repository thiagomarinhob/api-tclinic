package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate.CreateMedicalRecordTemplateRequest;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate.GetMedicalRecordTemplateByIdUseCase;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate.GetMedicalRecordTemplatesByTenantRequest;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate.GetMedicalRecordTemplatesByTenantUseCase;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate.MedicalRecordTemplateResponse;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate.CreateMedicalRecordTemplateUseCase;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate.UpdateMedicalRecordTemplateBody;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate.UpdateMedicalRecordTemplateRequest;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate.UpdateMedicalRecordTemplateUseCase;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate.DeleteMedicalRecordTemplateUseCase;
import com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate.SetMedicalRecordTemplateAsDefaultUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MedicalRecordTemplateController implements MedicalRecordTemplateAPI {

    private final CreateMedicalRecordTemplateUseCase createTemplateUseCase;
    private final GetMedicalRecordTemplateByIdUseCase getTemplateByIdUseCase;
    private final GetMedicalRecordTemplatesByTenantUseCase getTemplatesByTenantUseCase;
    private final UpdateMedicalRecordTemplateUseCase updateTemplateUseCase;
    private final DeleteMedicalRecordTemplateUseCase deleteTemplateUseCase;
    private final SetMedicalRecordTemplateAsDefaultUseCase setAsDefaultUseCase;

    @Override
    public MedicalRecordTemplateResponse createTemplate(@Valid @RequestBody CreateMedicalRecordTemplateRequest request) throws AuthenticationFailedException {
        return createTemplateUseCase.execute(request);
    }

    @Override
    public MedicalRecordTemplateResponse getTemplateById(@PathVariable UUID id) throws AuthenticationFailedException {
        return getTemplateByIdUseCase.execute(id);
    }

    @Override
    public List<MedicalRecordTemplateResponse> getTemplatesByTenant(
            @RequestParam UUID tenantId,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly,
            @RequestParam(required = false) String professionalType,
            @RequestParam(required = false) UUID professionalId
    ) throws AuthenticationFailedException {
        return getTemplatesByTenantUseCase.execute(
                new GetMedicalRecordTemplatesByTenantRequest(tenantId, activeOnly, professionalType, professionalId));
    }

    @Override
    public MedicalRecordTemplateResponse updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMedicalRecordTemplateBody body
    ) throws AuthenticationFailedException {
        return updateTemplateUseCase.execute(new UpdateMedicalRecordTemplateRequest(
                id, body.name(), body.professionalType(), body.schema(), body.active()));
    }

    @Override
    public MedicalRecordTemplateResponse setAsDefault(@PathVariable UUID id) throws AuthenticationFailedException {
        return setAsDefaultUseCase.execute(id);
    }

    @Override
    public void deleteTemplate(@PathVariable UUID id) throws AuthenticationFailedException {
        deleteTemplateUseCase.execute(id);
    }
}

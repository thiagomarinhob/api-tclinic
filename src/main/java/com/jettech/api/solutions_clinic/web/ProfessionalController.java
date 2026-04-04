package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.entity.DocumentType;
import com.jettech.api.solutions_clinic.model.entity.Specialty;
import com.jettech.api.solutions_clinic.model.usecase.professional.AddProfessionalToClinicBodyRequest;
import com.jettech.api.solutions_clinic.model.usecase.professional.AddProfessionalToClinicRequest;
import com.jettech.api.solutions_clinic.model.usecase.professional.CreateProfessionalRequest;
import com.jettech.api.solutions_clinic.model.usecase.professional.CreateProfessionalWithUserRequest;
import com.jettech.api.solutions_clinic.model.usecase.professional.AddProfessionalToClinicUseCase;
import com.jettech.api.solutions_clinic.model.usecase.professional.CreateProfessionalUseCase;
import com.jettech.api.solutions_clinic.model.usecase.professional.CreateProfessionalWithUserUseCase;
import com.jettech.api.solutions_clinic.model.usecase.professional.GetProfessionalsByClinicUseCase;
import com.jettech.api.solutions_clinic.model.usecase.professional.GetProfessionalTenantsUseCase;
import com.jettech.api.solutions_clinic.model.usecase.professional.GetProfessionalByUserIdUseCase;
import com.jettech.api.solutions_clinic.model.usecase.professional.UpdateProfessionalActiveUseCase;
import com.jettech.api.solutions_clinic.model.usecase.professional.UpdateProfessionalBodyRequest;
import com.jettech.api.solutions_clinic.model.usecase.professional.UpdateProfessionalRequest;
import com.jettech.api.solutions_clinic.model.usecase.professional.UpdateProfessionalUseCase;
import com.jettech.api.solutions_clinic.model.usecase.professional.GetProfessionalsByClinicRequest;
import com.jettech.api.solutions_clinic.model.usecase.professional.ProfessionalResponse;
import com.jettech.api.solutions_clinic.model.usecase.professional.ProfessionalTenantResponse;
import com.jettech.api.solutions_clinic.model.usecase.professional.UpdateProfessionalActiveBodyRequest;
import com.jettech.api.solutions_clinic.model.usecase.professional.UpdateProfessionalActiveRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ProfessionalController implements ProfessionalAPI {

    private final CreateProfessionalUseCase createProfessionalUseCase;
    private final CreateProfessionalWithUserUseCase createProfessionalWithUserUseCase;
    private final AddProfessionalToClinicUseCase addProfessionalToClinicUseCase;
    private final GetProfessionalTenantsUseCase getProfessionalTenantsUseCase;
    private final GetProfessionalsByClinicUseCase getProfessionalsByClinicUseCase;
    private final GetProfessionalByUserIdUseCase getProfessionalByUserIdUseCase;
    private final UpdateProfessionalActiveUseCase updateProfessionalActiveUseCase;
    private final UpdateProfessionalUseCase updateProfessionalUseCase;

    @Override
    public List<Specialty> getSpecialties() {
        return Arrays.asList(Specialty.values());
    }

    @Override
    public ProfessionalResponse createProfessional(@Valid @RequestBody CreateProfessionalRequest request) throws AuthenticationFailedException {
        return createProfessionalUseCase.execute(request);
    }

    @Override
    public ProfessionalResponse addProfessionalToClinic(
            @PathVariable UUID clinicId,
            @Valid @RequestBody AddProfessionalToClinicBodyRequest request) throws AuthenticationFailedException {
        return addProfessionalToClinicUseCase.execute(new AddProfessionalToClinicRequest(
                request.userId(),
                clinicId,
                request.specialty(),
                request.documentType(),
                request.documentNumber(),
                request.documentState(),
                request.bio()
        ));
    }

    @Override
    public ProfessionalResponse getProfessionalByUserId(@PathVariable UUID userId) throws AuthenticationFailedException {
        return getProfessionalByUserIdUseCase.execute(userId);
    }

    @Override
    public ProfessionalTenantResponse getProfessionalTenants(@PathVariable UUID userId) throws AuthenticationFailedException {
        return getProfessionalTenantsUseCase.execute(userId);
    }

    @Override
    public Page<ProfessionalResponse> getProfessionalsByClinic(
            @PathVariable UUID clinicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "user.fullName,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String documentType
    ) throws AuthenticationFailedException {
        DocumentType docType = null;
        if (documentType != null && !documentType.isEmpty()) {
            try {
                docType = DocumentType.valueOf(documentType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Se o tipo não for válido, mantém como null (todos os tipos)
            }
        }
        
        return getProfessionalsByClinicUseCase.execute(new GetProfessionalsByClinicRequest(
                clinicId,
                page,
                size,
                sort,
                search,
                active,
                docType
        ));
    }

    @Override
    public ProfessionalResponse updateProfessional(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfessionalBodyRequest request) throws AuthenticationFailedException {
        return updateProfessionalUseCase.execute(new UpdateProfessionalRequest(
                id,
                request.specialty(),
                request.documentType(),
                request.documentNumber(),
                request.documentState(),
                request.bio()
        ));
    }

    @Override
    public ProfessionalResponse updateProfessionalActive(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfessionalActiveBodyRequest request) throws AuthenticationFailedException {
        return updateProfessionalActiveUseCase.execute(new UpdateProfessionalActiveRequest(
                id,
                request.active()
        ));
    }

    @Override
    public ProfessionalResponse createProfessionalWithUser(@Valid @RequestBody CreateProfessionalWithUserRequest request) throws AuthenticationFailedException {
        return createProfessionalWithUserUseCase.execute(request);
    }
}


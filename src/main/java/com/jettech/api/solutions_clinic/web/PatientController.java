package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.patient.CreatePatientRequest;
import com.jettech.api.solutions_clinic.model.usecase.patient.CreatePatientUseCase;
import com.jettech.api.solutions_clinic.model.usecase.patient.GetPatientByIdUseCase;
import com.jettech.api.solutions_clinic.model.usecase.patient.GetPatientsByTenantUseCase;
import com.jettech.api.solutions_clinic.model.usecase.patient.GetPatientsByTenantRequest;
import com.jettech.api.solutions_clinic.model.usecase.patient.PatientResponse;
import com.jettech.api.solutions_clinic.model.usecase.patient.UpdatePatientActiveBodyRequest;
import com.jettech.api.solutions_clinic.model.usecase.patient.UpdatePatientActiveRequest;
import com.jettech.api.solutions_clinic.model.usecase.patient.UpdatePatientActiveUseCase;
import com.jettech.api.solutions_clinic.model.usecase.patient.UpdatePatientBodyRequest;
import com.jettech.api.solutions_clinic.model.usecase.patient.UpdatePatientRequest;
import com.jettech.api.solutions_clinic.model.usecase.patient.UpdatePatientUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PatientController implements PatientAPI {

    private final CreatePatientUseCase createPatientUseCase;
    private final GetPatientByIdUseCase getPatientByIdUseCase;
    private final GetPatientsByTenantUseCase getPatientsByTenantUseCase;
    private final UpdatePatientUseCase updatePatientUseCase;
    private final UpdatePatientActiveUseCase updatePatientActiveUseCase;

    @Override
    public PatientResponse createPatient(@Valid @RequestBody CreatePatientRequest request) throws AuthenticationFailedException {
        return createPatientUseCase.execute(request);
    }

    @Override
    public Page<PatientResponse> getPatientsByTenant(
            @RequestParam UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "firstName,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active) throws AuthenticationFailedException {
        return getPatientsByTenantUseCase.execute(new GetPatientsByTenantRequest(tenantId, page, size, sort, search, active));
    }

    @Override
    public PatientResponse getPatientById(@PathVariable UUID id) throws AuthenticationFailedException {
        return getPatientByIdUseCase.execute(id);
    }

    @Override
    public PatientResponse updatePatient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientBodyRequest request) throws AuthenticationFailedException {
        return updatePatientUseCase.execute(new UpdatePatientRequest(
                id,
                request.firstName(),
                request.motherName(),
                request.cpf(),
                request.birthDate(),
                request.gender(),
                request.email(),
                request.phone(),
                request.whatsapp(),
                request.addressStreet(),
                request.addressNumber(),
                request.addressComplement(),
                request.addressNeighborhood(),
                request.addressCity(),
                request.addressState(),
                request.addressZipcode(),
                request.bloodType(),
                request.allergies(),
                request.healthPlan(),
                request.guardianName(),
                request.guardianPhone(),
                request.guardianRelationship()
        ));
    }

    @Override
    public PatientResponse updatePatientActive(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientActiveBodyRequest request) throws AuthenticationFailedException {
        return updatePatientActiveUseCase.execute(new UpdatePatientActiveRequest(id, request.active()));
    }
}


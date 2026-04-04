package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.entity.LabOrderStatus;
import com.jettech.api.solutions_clinic.model.usecase.laboratory.*;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LabController implements LabAPI {

    private final CreateLabExamTypeUseCase createLabExamTypeUseCase;
    private final GetLabExamTypesByTenantUseCase getLabExamTypesByTenantUseCase;
    private final UpdateLabExamTypeUseCase updateLabExamTypeUseCase;
    private final DeleteLabExamTypeUseCase deleteLabExamTypeUseCase;
    private final CreateLabOrderUseCase createLabOrderUseCase;
    private final GetLabOrdersByTenantUseCase getLabOrdersByTenantUseCase;
    private final GetLabOrderByIdUseCase getLabOrderByIdUseCase;
    private final UpdateLabOrderStatusUseCase updateLabOrderStatusUseCase;
    private final GetLabOrdersByPatientUseCase getLabOrdersByPatientUseCase;
    private final EnterLabResultUseCase enterLabResultUseCase;
    private final ValidateLabResultUseCase validateLabResultUseCase;
    private final GetLabDashboardUseCase getLabDashboardUseCase;

    @Override
    public LabExamTypeResponse createExamType(@Valid @RequestBody CreateLabExamTypeRequest request) throws AuthenticationFailedException {
        return createLabExamTypeUseCase.execute(request);
    }

    @Override
    public Page<LabExamTypeResponse> getExamTypes(int page, int size, String search, Boolean active) throws AuthenticationFailedException {
        return getLabExamTypesByTenantUseCase.execute(new GetLabExamTypesByTenantRequest(page, size, search, active));
    }

    @Override
    public LabExamTypeResponse updateExamType(@PathVariable UUID id, @Valid @RequestBody UpdateLabExamTypeBodyRequest request) throws AuthenticationFailedException {
        return updateLabExamTypeUseCase.execute(new UpdateLabExamTypeRequest(
            id, request.code(), request.name(), request.sector(), request.sampleType(),
            request.unit(), request.referenceRangeText(), request.preparationInfo(),
            request.turnaroundHours(), request.active()
        ));
    }

    @Override
    public void deleteExamType(@PathVariable UUID id) throws AuthenticationFailedException {
        deleteLabExamTypeUseCase.execute(id);
    }

    @Override
    public LabOrderResponse createOrder(@Valid @RequestBody CreateLabOrderBodyRequest request) throws AuthenticationFailedException {
        return createLabOrderUseCase.execute(request);
    }

    @Override
    public Page<LabOrderResponse> getOrders(int page, int size, UUID patientId, String status, String search) throws AuthenticationFailedException {
        LabOrderStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = LabOrderStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return getLabOrdersByTenantUseCase.execute(new GetLabOrdersByTenantRequest(page, size, patientId, statusEnum, search));
    }

    @Override
    public LabOrderResponse getOrderById(@PathVariable UUID id) throws AuthenticationFailedException {
        return getLabOrderByIdUseCase.execute(id);
    }

    @Override
    public LabOrderResponse updateOrderStatus(@PathVariable UUID id, @Valid @RequestBody UpdateLabOrderStatusBodyRequest request) throws AuthenticationFailedException {
        return updateLabOrderStatusUseCase.execute(new UpdateLabOrderStatusRequest(
            id, request.status(), request.sampleCode(), request.collectedBy(),
            request.collectedAt(), request.receivedAt()
        ));
    }

    @Override
    public List<LabOrderResponse> getOrdersByPatient(@PathVariable UUID patientId) throws AuthenticationFailedException {
        return getLabOrdersByPatientUseCase.execute(patientId);
    }

    @Override
    public LabOrderItemResponse enterResult(@PathVariable UUID itemId, @Valid @RequestBody EnterLabResultBodyRequest request) throws AuthenticationFailedException {
        return enterLabResultUseCase.execute(new EnterLabResultRequest(
            itemId, request.resultValue(), request.abnormal(), request.critical(), request.observations()
        ));
    }

    @Override
    public LabOrderItemResponse validateResult(@PathVariable UUID itemId, @Valid @RequestBody ValidateLabResultBodyRequest request) throws AuthenticationFailedException {
        return validateLabResultUseCase.execute(new ValidateLabResultRequest(
            itemId, request.validationType(), request.validatedBy()
        ));
    }

    @Override
    public LabDashboardResponse getDashboard() throws AuthenticationFailedException {
        return getLabDashboardUseCase.execute();
    }
}

package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.repository.LabOrderRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetLabOrdersByPatientUseCase implements GetLabOrdersByPatientUseCase {

    private final LabOrderRepository labOrderRepository;
    private final TenantContext tenantContext;

    @Override
    public List<LabOrderResponse> execute(UUID patientId) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        log.info("Listando pedidos laboratoriais do paciente - tenantId: {}, patientId: {}", tenantId, patientId);
        var result = labOrderRepository
            .findByPatientIdAndTenantIdOrderByCreatedAtDesc(patientId, tenantId)
            .stream()
            .map(DefaultCreateLabOrderUseCase::toResponse)
            .collect(Collectors.toList());
        log.info("Pedidos laboratoriais encontrados para patientId: {} - total: {}", patientId, result.size());
        return result;
    }
}

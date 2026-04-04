package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.*;
import com.jettech.api.solutions_clinic.model.repository.*;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateLabOrderUseCase implements CreateLabOrderUseCase {

    private final LabOrderRepository labOrderRepository;
    private final LabExamTypeRepository labExamTypeRepository;
    private final PatientRepository patientRepository;
    private final TenantRepository tenantRepository;
    private final ProfessionalRepository professionalRepository;
    private final AppointmentRepository appointmentRepository;
    private final HealthPlanRepository healthPlanRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public LabOrderResponse execute(CreateLabOrderBodyRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente", request.patientId()));
        if (!patient.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }

        Professional professional = null;
        if (request.professionalId() != null) {
            professional = professionalRepository.findById(request.professionalId())
                    .orElseThrow(() -> new EntityNotFoundException("Profissional", request.professionalId()));
        }

        Appointment appointment = null;
        if (request.appointmentId() != null) {
            appointment = appointmentRepository.findById(request.appointmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Consulta", request.appointmentId()));
        }

        HealthPlan healthPlan = null;
        if (request.healthPlanId() != null) {
            healthPlan = healthPlanRepository.findByIdAndTenantId(request.healthPlanId(), tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Convênio", request.healthPlanId()));
        }

        LabOrder order = new LabOrder();
        order.setTenant(tenant);
        order.setPatient(patient);
        order.setProfessional(professional);
        order.setAppointment(appointment);
        order.setRequesterName(request.requesterName());
        order.setPriority(request.priority() != null ? request.priority() : LabPriority.ROUTINE);
        order.setPaymentType(request.paymentType() != null ? request.paymentType() : LabPaymentType.PIX);
        order.setHealthPlan(healthPlan);
        order.setHealthPlanName(healthPlan != null ? healthPlan.getName() : null);
        order.setClinicalNotes(request.clinicalNotes());
        order.setStatus(LabOrderStatus.REQUESTED);

        List<LabOrderItem> items = new ArrayList<>();
        for (CreateLabOrderItemRequest itemReq : request.items()) {
            LabOrderItem item = new LabOrderItem();
            item.setOrder(order);
            if (itemReq.examTypeId() != null) {
                labExamTypeRepository.findById(itemReq.examTypeId()).ifPresent(item::setExamType);
            }
            item.setExamName(itemReq.examName());
            item.setSector(itemReq.sector() != null ? itemReq.sector() : LabSector.OTHER);
            item.setSampleType(itemReq.sampleType());
            item.setUnit(itemReq.unit());
            item.setReferenceRangeText(itemReq.referenceRangeText());
            item.setResultStatus(LabResultStatus.PENDING);
            items.add(item);
        }
        order.setItems(items);

        order = labOrderRepository.save(order);
        return toResponse(order);
    }

    static LabOrderResponse toResponse(LabOrder o) {
        List<LabOrderItemResponse> itemResponses = o.getItems().stream()
            .map(DefaultCreateLabOrderUseCase::toItemResponse)
            .collect(Collectors.toList());
        return new LabOrderResponse(
            o.getId(), o.getTenant().getId(),
            o.getPatient().getId(), o.getPatient().getFirstName(),
            o.getAppointment() != null ? o.getAppointment().getId() : null,
            o.getProfessional() != null ? o.getProfessional().getId() : null,
            o.getRequesterName(), o.getPriority(), o.getPaymentType(),
            o.getHealthPlan() != null ? o.getHealthPlan().getId() : null,
            o.getHealthPlanName(), o.getClinicalNotes(), o.getStatus(),
            o.getSampleCode(), o.getCollectedAt(), o.getCollectedBy(),
            o.getReceivedAt(), itemResponses, o.getCreatedAt(), o.getUpdatedAt()
        );
    }

    static LabOrderItemResponse toItemResponse(LabOrderItem i) {
        return new LabOrderItemResponse(
            i.getId(),
            i.getExamType() != null ? i.getExamType().getId() : null,
            i.getExamName(), i.getSector(), i.getSampleType(), i.getUnit(),
            i.getReferenceRangeText(), i.getResultValue(), i.getResultStatus(),
            i.getAbnormal(), i.isCritical(),
            i.getTechnicalValidatedBy(), i.getTechnicalValidatedAt(),
            i.getFinalValidatedBy(), i.getFinalValidatedAt(),
            i.getObservations(), i.getCreatedAt(), i.getUpdatedAt()
        );
    }
}

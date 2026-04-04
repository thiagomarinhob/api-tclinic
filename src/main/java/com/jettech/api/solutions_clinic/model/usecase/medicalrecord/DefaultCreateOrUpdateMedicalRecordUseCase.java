package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.MedicalRecord;
import com.jettech.api.solutions_clinic.model.entity.MedicalRecordTemplate;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.MedicalRecordRepository;
import com.jettech.api.solutions_clinic.model.repository.MedicalRecordTemplateRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateOrUpdateMedicalRecordUseCase implements CreateOrUpdateMedicalRecordUseCase {

    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordTemplateRepository templateRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionalRepository;
    private final TenantContext tenantContext;
    private final ObjectMapper objectMapper;
    private final MedicalRecordResponseMapper responseMapper;

    @Override
    @Transactional
    public MedicalRecordResponse execute(CreateOrUpdateMedicalRecordRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();

        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", request.appointmentId()));
        if (!appointment.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException(com.jettech.api.solutions_clinic.exception.ApiError.ACCESS_DENIED);
        }

        UUID professionalId = tenantContext.getUserIdOrNull() != null
                ? professionalRepository.findByUserIdAndTenantId(tenantContext.getUserIdOrNull(), tenantId)
                        .map(p -> p.getId())
                        .orElse(null)
                : null;
        MedicalRecordTemplate template = templateRepository.findByIdAvailableForTenant(
                        request.templateId(), tenantId, professionalId)
                .orElseThrow(() -> new EntityNotFoundException("Modelo de prontuário", request.templateId()));

        MedicalRecord record = medicalRecordRepository.findByAppointmentId(request.appointmentId())
                .orElseGet(() -> {
                    MedicalRecord newRecord = new MedicalRecord();
                    newRecord.setAppointment(appointment);
                    newRecord.setTemplate(template);
                    return newRecord;
                });

        record.setTemplate(template);
        record.setContent(toJsonNode(request.content()));
        record.setVitalSigns(request.vitalSigns() != null ? toJsonNode(request.vitalSigns()) : null);

        record = medicalRecordRepository.save(record);
        return responseMapper.toResponse(record);
    }

    private JsonNode toJsonNode(Map<String, Object> map) {
        return objectMapper.valueToTree(map);
    }
}

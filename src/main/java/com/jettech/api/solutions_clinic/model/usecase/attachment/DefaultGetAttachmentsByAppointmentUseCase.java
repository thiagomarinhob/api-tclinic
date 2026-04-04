package com.jettech.api.solutions_clinic.model.usecase.attachment;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentAttachmentRepository;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetAttachmentsByAppointmentUseCase implements GetAttachmentsByAppointmentUseCase {

    private final AppointmentAttachmentRepository attachmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> execute(UUID appointmentId) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", appointmentId));
        if (!appointment.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }

        return attachmentRepository
                .findByAppointmentIdOrderByCreatedAtDesc(appointmentId)
                .stream()
                .map(DefaultConfirmAttachmentUploadUseCase::toResponse)
                .toList();
    }
}

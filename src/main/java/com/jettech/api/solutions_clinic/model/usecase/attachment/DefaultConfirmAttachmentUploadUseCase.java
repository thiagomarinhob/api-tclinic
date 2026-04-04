package com.jettech.api.solutions_clinic.model.usecase.attachment;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentAttachment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentAttachmentRepository;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultConfirmAttachmentUploadUseCase implements ConfirmAttachmentUploadUseCase {

    private final AppointmentAttachmentRepository attachmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public AttachmentResponse execute(ConfirmAttachmentUploadRequest request)
            throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", request.appointmentId()));
        if (!appointment.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }

        AppointmentAttachment attachment = new AppointmentAttachment();
        attachment.setTenant(appointment.getTenant());
        attachment.setAppointment(appointment);
        attachment.setFileName(request.fileName());
        attachment.setObjectKey(request.objectKey());
        attachment.setFileType(request.fileType());
        attachment.setFileSizeBytes(request.fileSizeBytes());
        attachment = attachmentRepository.save(attachment);

        return toResponse(attachment);
    }

    static AttachmentResponse toResponse(AppointmentAttachment a) {
        return new AttachmentResponse(
                a.getId(),
                a.getAppointment().getId(),
                a.getFileName(),
                a.getFileType(),
                a.getFileSizeBytes(),
                a.getCreatedAt()
        );
    }
}

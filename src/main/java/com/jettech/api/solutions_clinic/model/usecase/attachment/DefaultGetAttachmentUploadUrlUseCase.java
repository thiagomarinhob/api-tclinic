package com.jettech.api.solutions_clinic.model.usecase.attachment;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.exception.ServiceUnavailableException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.service.R2StorageService;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetAttachmentUploadUrlUseCase implements GetAttachmentUploadUrlUseCase {

    private static final int PRESIGNED_EXPIRY_MINUTES = 5;

    private final AppointmentRepository appointmentRepository;
    private final R2StorageService r2StorageService;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public AttachmentUploadUrlResponse execute(GetAttachmentUploadUrlRequest request)
            throws AuthenticationFailedException {
        if (!r2StorageService.isConfigured()) {
            throw new ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }

        UUID tenantId = tenantContext.getRequiredClinicId();
        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", request.appointmentId()));
        if (!appointment.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }

        String uniqueFileName = UUID.randomUUID() + "_" + sanitizeFileName(request.effectiveFileName());
        String objectKey = "tenants/%s/appointments/%s/attachments/%s"
                .formatted(tenantId, appointment.getId(), uniqueFileName);

        String uploadUrl = r2StorageService.createPresignedPutUrl(objectKey, PRESIGNED_EXPIRY_MINUTES);
        if (uploadUrl == null) {
            throw new ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }

        return new AttachmentUploadUrlResponse(uploadUrl, objectKey, PRESIGNED_EXPIRY_MINUTES);
    }

    private static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "anexo.pdf";
        String safe = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        return safe.length() > 200 ? safe.substring(0, 200) : safe;
    }
}

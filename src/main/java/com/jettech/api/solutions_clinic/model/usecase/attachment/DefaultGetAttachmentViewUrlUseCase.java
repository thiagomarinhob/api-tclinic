package com.jettech.api.solutions_clinic.model.usecase.attachment;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.exception.ServiceUnavailableException;
import com.jettech.api.solutions_clinic.model.entity.AppointmentAttachment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentAttachmentRepository;
import com.jettech.api.solutions_clinic.model.service.R2StorageService;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetAttachmentViewUrlUseCase implements GetAttachmentViewUrlUseCase {

    private static final int VIEW_EXPIRY_MINUTES = 15;

    private final AppointmentAttachmentRepository attachmentRepository;
    private final R2StorageService r2StorageService;
    private final TenantContext tenantContext;

    @Override
    @Transactional(readOnly = true)
    public AttachmentViewUrlResponse execute(UUID attachmentId) throws AuthenticationFailedException {
        if (!r2StorageService.isConfigured()) {
            throw new ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }

        UUID tenantId = tenantContext.getRequiredClinicId();
        AppointmentAttachment attachment = attachmentRepository.findByIdAndTenantId(attachmentId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Anexo", attachmentId));
        if (!attachment.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }

        String url = r2StorageService.createPresignedGetUrl(attachment.getObjectKey(), VIEW_EXPIRY_MINUTES);
        if (url == null) {
            throw new ServiceUnavailableException(ApiError.R2_NOT_CONFIGURED);
        }

        return new AttachmentViewUrlResponse(url);
    }
}

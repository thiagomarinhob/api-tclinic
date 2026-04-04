package com.jettech.api.solutions_clinic.model.usecase.attachment;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.repository.AppointmentAttachmentRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultDeleteAttachmentUseCase implements DeleteAttachmentUseCase {

    private final AppointmentAttachmentRepository attachmentRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public Void execute(UUID attachmentId) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        var attachment = attachmentRepository.findByIdAndTenantId(attachmentId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Anexo", attachmentId));
        attachmentRepository.delete(attachment);
        return null;
    }
}

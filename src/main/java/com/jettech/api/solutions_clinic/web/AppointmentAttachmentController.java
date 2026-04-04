package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.usecase.attachment.AttachmentResponse;
import com.jettech.api.solutions_clinic.model.usecase.attachment.AttachmentUploadUrlResponse;
import com.jettech.api.solutions_clinic.model.usecase.attachment.AttachmentViewUrlResponse;
import com.jettech.api.solutions_clinic.model.usecase.attachment.ConfirmAttachmentUploadRequest;
import com.jettech.api.solutions_clinic.model.usecase.attachment.ConfirmAttachmentUploadUseCase;
import com.jettech.api.solutions_clinic.model.usecase.attachment.DeleteAttachmentUseCase;
import com.jettech.api.solutions_clinic.model.usecase.attachment.GetAttachmentUploadUrlRequest;
import com.jettech.api.solutions_clinic.model.usecase.attachment.GetAttachmentUploadUrlUseCase;
import com.jettech.api.solutions_clinic.model.usecase.attachment.GetAttachmentViewUrlUseCase;
import com.jettech.api.solutions_clinic.model.usecase.attachment.GetAttachmentsByAppointmentUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AppointmentAttachmentController implements AppointmentAttachmentAPI {

    private final GetAttachmentUploadUrlUseCase getAttachmentUploadUrlUseCase;
    private final ConfirmAttachmentUploadUseCase confirmAttachmentUploadUseCase;
    private final GetAttachmentsByAppointmentUseCase getAttachmentsByAppointmentUseCase;
    private final GetAttachmentViewUrlUseCase getAttachmentViewUrlUseCase;
    private final DeleteAttachmentUseCase deleteAttachmentUseCase;

    @Override
    public AttachmentUploadUrlResponse getUploadUrl(
            @PathVariable UUID appointmentId,
            @RequestParam(required = false) String fileName) throws AuthenticationFailedException {
        return getAttachmentUploadUrlUseCase.execute(new GetAttachmentUploadUrlRequest(appointmentId, fileName));
    }

    @Override
    public AttachmentResponse confirmUpload(
            @Valid @RequestBody ConfirmAttachmentUploadRequest request) throws AuthenticationFailedException {
        return confirmAttachmentUploadUseCase.execute(request);
    }

    @Override
    public List<AttachmentResponse> listByAppointment(
            @PathVariable UUID appointmentId) throws AuthenticationFailedException {
        return getAttachmentsByAppointmentUseCase.execute(appointmentId);
    }

    @Override
    public AttachmentViewUrlResponse getViewUrl(
            @PathVariable UUID attachmentId) throws AuthenticationFailedException {
        return getAttachmentViewUrlUseCase.execute(attachmentId);
    }

    @Override
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable UUID attachmentId) throws AuthenticationFailedException {
        deleteAttachmentUseCase.execute(attachmentId);
        return ResponseEntity.noContent().build();
    }
}

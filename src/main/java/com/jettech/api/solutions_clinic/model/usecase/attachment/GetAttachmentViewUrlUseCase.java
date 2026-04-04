package com.jettech.api.solutions_clinic.model.usecase.attachment;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import java.util.UUID;

public interface GetAttachmentViewUrlUseCase
        extends UseCase<UUID, AttachmentViewUrlResponse> {}

package com.jettech.api.solutions_clinic.model.usecase.attachment;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import java.util.List;
import java.util.UUID;

public interface GetAttachmentsByAppointmentUseCase
        extends UseCase<UUID, List<AttachmentResponse>> {}

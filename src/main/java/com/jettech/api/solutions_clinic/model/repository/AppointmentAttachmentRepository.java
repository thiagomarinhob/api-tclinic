package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.AppointmentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentAttachmentRepository extends JpaRepository<AppointmentAttachment, UUID> {

    List<AppointmentAttachment> findByAppointmentIdOrderByCreatedAtDesc(UUID appointmentId);

    Optional<AppointmentAttachment> findByIdAndTenantId(UUID id, UUID tenantId);
}

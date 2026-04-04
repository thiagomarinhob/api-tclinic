package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.AppointmentProcedure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentProcedureRepository extends JpaRepository<AppointmentProcedure, UUID> {

    List<AppointmentProcedure> findByAppointmentId(UUID appointmentId);

    List<AppointmentProcedure> findByProcedureId(UUID procedureId);

    void deleteByAppointmentId(UUID appointmentId);
}

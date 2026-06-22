package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Fábrica de entidades para testes do domínio de Agendamento.
 * Todos os IDs são fixos para facilitar asserções nos testes.
 */
class AppointmentFixtures {

    static final UUID TENANT_ID       = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final UUID OTHER_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
    static final UUID PATIENT_ID      = UUID.fromString("00000000-0000-0000-0000-000000000002");
    static final UUID PROFESSIONAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    static final UUID ROOM_ID         = UUID.fromString("00000000-0000-0000-0000-000000000004");
    static final UUID APPOINTMENT_ID  = UUID.fromString("00000000-0000-0000-0000-000000000005");
    static final UUID CREATED_BY_ID   = UUID.fromString("00000000-0000-0000-0000-000000000006");
    static final UUID PROCEDURE_ID    = UUID.fromString("00000000-0000-0000-0000-000000000007");

    static final LocalDateTime SCHEDULED_AT = LocalDateTime.of(2026, 4, 14, 10, 0); // Segunda-feira
    static final int DURATION_MINUTES = 30;
    static final BigDecimal TOTAL_VALUE = new BigDecimal("150.00");

    static Tenant tenant() {
        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);
        return tenant;
    }

    static Tenant otherTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(OTHER_TENANT_ID);
        return tenant;
    }

    static User user() {
        User user = new User();
        user.setId(CREATED_BY_ID);
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setEmail("admin@clinica.com");
        return user;
    }

    static Patient patient() {
        Patient patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setFirstName("João");
        patient.setTenant(tenant());
        return patient;
    }

    static Professional professional() {
        return professional(tenant());
    }

    static Professional professional(Tenant tenant) {
        User profUser = new User();
        profUser.setId(UUID.randomUUID());
        profUser.setFirstName("Dra. Maria");
        profUser.setEmail("maria@clinica.com");

        Professional professional = new Professional();
        professional.setId(PROFESSIONAL_ID);
        professional.setTenant(tenant);
        professional.setUser(profUser);
        return professional;
    }

    static Room room() {
        Room room = new Room();
        room.setId(ROOM_ID);
        return room;
    }

    static Appointment appointment() {
        return appointment(tenant(), patient(), professional(tenant()));
    }

    static Appointment appointment(Tenant tenant, Patient patient, Professional professional) {
        Appointment appointment = new Appointment();
        appointment.setId(APPOINTMENT_ID);
        appointment.setTenant(tenant);
        appointment.setPatient(patient);
        appointment.setProfessional(professional);
        appointment.setScheduledAt(SCHEDULED_AT);
        appointment.setDurationMinutes(DURATION_MINUTES);
        appointment.setStatus(AppointmentStatus.AGENDADO);
        appointment.setTotalValue(TOTAL_VALUE);
        appointment.setPaymentStatus(PaymentStatus.PENDENTE);
        appointment.setCreatedBy(user());
        appointment.setProcedures(new ArrayList<>());
        return appointment;
    }

    static Procedure procedure() {
        return procedure(PROCEDURE_ID, true, 30, new BigDecimal("150.00"));
    }

    static Procedure procedure(UUID id, boolean active, int durationMinutes, BigDecimal basePrice) {
        Procedure procedure = new Procedure();
        procedure.setId(id);
        procedure.setName("Consulta");
        procedure.setDescription("Consulta geral");
        procedure.setActive(active);
        procedure.setEstimatedDurationMinutes(durationMinutes);
        procedure.setBasePrice(basePrice);
        procedure.setTenant(tenant());
        return procedure;
    }

    static AppointmentResponse appointmentResponse() {
        return new AppointmentResponse(
                APPOINTMENT_ID, TENANT_ID, PATIENT_ID, PROFESSIONAL_ID, null,
                null, null, null,
                SCHEDULED_AT, DURATION_MINUTES, AppointmentStatus.AGENDADO,
                null, null, null, null, null,
                TOTAL_VALUE, null, PaymentStatus.PENDENTE, null,
                CREATED_BY_ID, LocalDateTime.now(), LocalDateTime.now(),
                null, java.util.List.of(), null
        );
    }
}

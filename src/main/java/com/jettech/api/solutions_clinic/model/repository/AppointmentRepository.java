package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    
    List<Appointment> findByProfessionalId(UUID professionalId);
    
    List<Appointment> findByPatientId(UUID patientId);
    
    List<Appointment> findByRoomId(UUID roomId);
    
    List<Appointment> findByProfessionalIdAndScheduledAtBetween(
            UUID professionalId, 
            LocalDateTime start, 
            LocalDateTime end
    );
    
    List<Appointment> findByProfessionalIdAndScheduledAtBetweenAndStatusNot(
            UUID professionalId,
            LocalDateTime start,
            LocalDateTime end,
            AppointmentStatus status
    );
    
    List<Appointment> findByRoomIdAndScheduledAtBetweenAndStatusNot(
            UUID roomId,
            LocalDateTime start,
            LocalDateTime end,
            AppointmentStatus status
    );
    
    Optional<Appointment> findByIdAndStatus(UUID id, AppointmentStatus status);
    
    // Métodos para buscar por tenant com filtros
    List<Appointment> findByTenantId(UUID tenantId);
    
    List<Appointment> findByTenantIdAndScheduledAtBetween(
            UUID tenantId,
            LocalDateTime start,
            LocalDateTime end
    );
    
    List<Appointment> findByTenantIdAndStatus(
            UUID tenantId,
            AppointmentStatus status
    );
    
    List<Appointment> findByTenantIdAndScheduledAtBetweenAndStatus(
            UUID tenantId,
            LocalDateTime start,
            LocalDateTime end,
            AppointmentStatus status
    );

    Optional<Appointment> findByWhatsappMessageId(String whatsappMessageId);

    /**
     * Busca agendamentos que iniciam dentro da janela informada e ainda não tiveram
     * o lembrete enviado. Usado pelo job de lembrete de 2h antes da consulta.
     */
    @Query("SELECT a FROM appointments a " +
           "WHERE a.scheduledAt BETWEEN :windowStart AND :windowEnd " +
           "AND a.status = :status " +
           "AND a.reminderSentAt IS NULL")
    List<Appointment> findAppointmentsForReminder(
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd")   LocalDateTime windowEnd,
            @Param("status")      AppointmentStatus status
    );

    /** Fallback: agendamento mais recente (AGENDADO ou CONFIRMADO) entre os pacientes informados. */
    Optional<Appointment> findFirstByPatientIdInAndStatusInOrderByScheduledAtDesc(
            List<UUID> patientIds,
            List<AppointmentStatus> statuses
    );
}

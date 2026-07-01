package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Override
    @EntityGraph(attributePaths = {
            "tenant",
            "patient",
            "professional",
            "professional.user",
            "room",
            "createdBy",
            "procedures",
            "procedures.procedure"
    })
    Optional<Appointment> findById(UUID id);
    
    @EntityGraph(attributePaths = {
            "tenant",
            "patient",
            "professional",
            "professional.user",
            "room",
            "createdBy",
            "procedures",
            "procedures.procedure"
    })
    List<Appointment> findByProfessionalId(UUID professionalId);
    
    List<Appointment> findByPatientId(UUID patientId);
    
    List<Appointment> findByRoomId(UUID roomId);
    
    @EntityGraph(attributePaths = {
            "tenant",
            "patient",
            "professional",
            "professional.user",
            "room",
            "createdBy",
            "procedures",
            "procedures.procedure"
    })
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
    @EntityGraph(attributePaths = {
            "tenant",
            "patient",
            "professional",
            "professional.user",
            "room",
            "createdBy",
            "procedures",
            "procedures.procedure"
    })
    List<Appointment> findByTenantId(UUID tenantId);
    
    @EntityGraph(attributePaths = {
            "tenant",
            "patient",
            "professional",
            "professional.user",
            "room",
            "createdBy",
            "procedures",
            "procedures.procedure"
    })
    List<Appointment> findByTenantIdAndScheduledAtBetween(
            UUID tenantId,
            LocalDateTime start,
            LocalDateTime end
    );
    
    @EntityGraph(attributePaths = {
            "tenant",
            "patient",
            "professional",
            "professional.user",
            "room",
            "createdBy",
            "procedures",
            "procedures.procedure"
    })
    List<Appointment> findByTenantIdAndStatus(
            UUID tenantId,
            AppointmentStatus status
    );
    
    @EntityGraph(attributePaths = {
            "tenant",
            "patient",
            "professional",
            "professional.user",
            "room",
            "createdBy",
            "procedures",
            "procedures.procedure"
    })
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

    /**
     * Fallback primário: agendamento cujo lembrete WhatsApp foi enviado mais recentemente.
     * Garante que, quando o paciente responde sem usar "reply", pegamos a mensagem que ele recebeu por último.
     */
    Optional<Appointment> findFirstByPatientIdInAndStatusInAndReminderSentAtIsNotNullOrderByReminderSentAtDesc(
            List<UUID> patientIds,
            List<AppointmentStatus> statuses
    );

    /** Fallback secundário: agendamento mais recente por data de agendamento (AGENDADO ou CONFIRMADO). */
    Optional<Appointment> findFirstByPatientIdInAndStatusInOrderByScheduledAtDesc(
            List<UUID> patientIds,
            List<AppointmentStatus> statuses
    );

    /** Usado ao gerar um novo código de confirmação, para evitar colisão entre agendamentos ativos. */
    boolean existsByConfirmationCodeAndStatusIn(String confirmationCode, List<AppointmentStatus> statuses);

    /** Todos os agendamentos ativos do(s) paciente(s) — usado para resolver ambiguidade via código quando há mais de um candidato. */
    List<Appointment> findByPatientIdInAndStatusIn(List<UUID> patientIds, List<AppointmentStatus> statuses);
}

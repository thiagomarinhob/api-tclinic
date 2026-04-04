package com.jettech.api.solutions_clinic.model.service;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.Notification;
import com.jettech.api.solutions_clinic.model.entity.NotificationType;
import com.jettech.api.solutions_clinic.model.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * Serviço para criar notificações in-app associadas a eventos (ex: confirmação e cancelamento de consultas).
 */
@Service
@RequiredArgsConstructor
public class NotificationCreator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createAppointmentConfirmation(Appointment appointment) {
        String patientName = appointment.getPatient().getFirstName() != null
                ? appointment.getPatient().getFirstName().trim()
                : "Paciente";
        String dateStr = appointment.getScheduledAt().format(DATE_FORMAT);
        String timeStr = appointment.getScheduledAt().format(TIME_FORMAT);

        Notification n = new Notification();
        n.setTenant(appointment.getTenant());
        n.setType(NotificationType.APPOINTMENT_CONFIRMATION);
        n.setTitle("Confirmação via WhatsApp");
        n.setDescription(String.format("%s confirmou a consulta para %s às %s", patientName, dateStr, timeStr));
        n.setReferenceType("appointment");
        n.setReferenceId(appointment.getId());
        notificationRepository.save(n);
    }

    @Transactional
    public void createAppointmentCancellation(Appointment appointment) {
        String patientName = appointment.getPatient().getFirstName() != null
                ? appointment.getPatient().getFirstName().trim()
                : "Paciente";
        String dateStr = appointment.getScheduledAt().format(DATE_FORMAT);
        String timeStr = appointment.getScheduledAt().format(TIME_FORMAT);

        Notification n = new Notification();
        n.setTenant(appointment.getTenant());
        n.setType(NotificationType.APPOINTMENT_CANCELLATION);
        n.setTitle("Desmarcação via WhatsApp");
        n.setDescription(String.format("%s desmarcou a consulta para %s às %s", patientName, dateStr, timeStr));
        n.setReferenceType("appointment");
        n.setReferenceId(appointment.getId());
        notificationRepository.save(n);
    }
}

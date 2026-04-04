package com.jettech.api.solutions_clinic.model.service;

import com.resend.services.emails.model.Tag;
import com.jettech.api.solutions_clinic.config.ResendConfig;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentEmailService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT  = DateTimeFormatter.ofPattern("HH:mm");

    private final EmailNotificationService emailNotificationService;
    private final ResendConfig resendConfig;

    // -------------------------------------------------------------------------
    // Ações públicas
    // -------------------------------------------------------------------------

    public void sendConfirmation(Appointment appointment) {
        dispatch(appointment, Action.CONFIRMADO);
    }

    public void sendUpdate(Appointment appointment) {
        dispatch(appointment, Action.ATUALIZADO);
    }

    public void sendCancellation(Appointment appointment) {
        dispatch(appointment, Action.CANCELADO);
    }

    // -------------------------------------------------------------------------
    // Dispatch central
    // -------------------------------------------------------------------------

    private void dispatch(Appointment appointment, Action action) {
        Patient patient = appointment.getPatient();
        if (!hasEmail(patient)) return;

        try {
            String subject = buildSubject(action, appointment);
            String html    = buildTemplate(action, appointment);
            List<Tag> tags = List.of(
                    Tag.builder().name("action").value(action.tagValue).build(),
                    Tag.builder().name("type").value("appointment").build()
            );
            emailNotificationService.sendEmail(resendConfig.getFrom(), patient.getEmail(), subject, html, tags);
            log.info("E-mail '{}' enviado para paciente {}", action.tagValue, patient.getId());
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail '{}' para paciente {}: {}", action.tagValue, patient.getId(), e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Assunto
    // -------------------------------------------------------------------------

    private String buildSubject(Action action, Appointment appointment) {
        String data = appointment.getScheduledAt().format(DATE_FMT);
        String hora = appointment.getScheduledAt().format(TIME_FMT);
        return switch (action) {
            case CONFIRMADO -> "Consulta confirmada – " + data + " às " + hora;
            case ATUALIZADO -> "Consulta reagendada – " + data + " às " + hora;
            case CANCELADO  -> "Consulta cancelada – " + data + " às " + hora;
        };
    }

    // -------------------------------------------------------------------------
    // Template HTML
    // -------------------------------------------------------------------------

    private String buildTemplate(Action action, Appointment appointment) {
        Patient patient    = appointment.getPatient();
        Tenant  tenant     = appointment.getTenant();
        String  data       = appointment.getScheduledAt().format(DATE_FMT);
        String  hora       = appointment.getScheduledAt().format(TIME_FMT);
        String  duracao    = formatDuration(appointment.getDurationMinutes());
        String  profissional = getProfessionalName(appointment);
        String  clinicName = tenant.getName() != null ? tenant.getName() : "";
        String  clinicPhone = tenant.getPhone() != null ? tenant.getPhone() : "";

        String badgeHtml   = buildBadge(action);
        String detailsHtml = buildDetailsTable(data, hora, duracao, profissional, patient, clinicName);
        String messageHtml = buildActionMessage(action, patient.getFirstName(), clinicPhone);

        return "<!DOCTYPE html>" +
            "<html lang=\"pt-BR\">" +
            "<head>" +
              "<meta charset=\"UTF-8\">" +
              "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">" +
              "<title>" + clinicName + "</title>" +
            "</head>" +
            "<body style=\"margin:0;padding:0;background-color:#f4f6f9;font-family:Arial,Helvetica,sans-serif;\">" +

            // Wrapper
            "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#f4f6f9;padding:32px 0;\">" +
            "<tr><td align=\"center\">" +

            // Card
            "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"" +
              "background-color:#ffffff;" +
              "border-radius:8px;" +
              "overflow:hidden;" +
              "box-shadow:0 2px 8px rgba(0,0,0,0.08);" +
              "max-width:600px;" +
              "width:100%;" +
            "\">" +

            // ── Header ────────────────────────────────────────────────────────
            "<tr>" +
            "<td style=\"background-color:#1d4ed8;padding:28px 32px;\">" +
              "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">" +
              "<tr>" +
                "<td>" +
                  "<p style=\"margin:0;font-size:20px;font-weight:bold;color:#ffffff;letter-spacing:0.5px;\">" +
                    escapeHtml(clinicName) +
                  "</p>" +
                  "<p style=\"margin:4px 0 0;font-size:13px;color:#bfdbfe;\">Sistema de Agendamentos</p>" +
                "</td>" +
                "<td align=\"right\">" +
                  "<div style=\"" +
                    "width:44px;height:44px;" +
                    "background-color:rgba(255,255,255,0.15);" +
                    "border-radius:50%;" +
                    "text-align:center;line-height:44px;" +
                    "font-size:22px;" +
                  "\">🏥</div>" +
                "</td>" +
              "</tr>" +
              "</table>" +
            "</td>" +
            "</tr>" +

            // ── Badge de ação ─────────────────────────────────────────────────
            "<tr>" +
            "<td style=\"padding:28px 32px 8px;\">" +
              badgeHtml +
            "</td>" +
            "</tr>" +

            // ── Saudação ──────────────────────────────────────────────────────
            "<tr>" +
            "<td style=\"padding:8px 32px 0;\">" +
              "<p style=\"margin:0;font-size:15px;color:#374151;\">" +
                "Olá, <strong>" + escapeHtml(patient.getFirstName()) + "</strong>!" +
              "</p>" +
              "<p style=\"margin:8px 0 0;font-size:14px;color:#6b7280;line-height:1.6;\">" +
                messageHtml +
              "</p>" +
            "</td>" +
            "</tr>" +

            // ── Detalhes do agendamento ───────────────────────────────────────
            "<tr>" +
            "<td style=\"padding:20px 32px;\">" +
              "<p style=\"margin:0 0 12px;font-size:13px;font-weight:bold;color:#6b7280;" +
                "text-transform:uppercase;letter-spacing:0.8px;\">Detalhes do Agendamento</p>" +
              detailsHtml +
            "</td>" +
            "</tr>" +

            // ── Separador ─────────────────────────────────────────────────────
            "<tr>" +
            "<td style=\"padding:0 32px;\">" +
              "<hr style=\"border:none;border-top:1px solid #e5e7eb;margin:0;\">" +
            "</td>" +
            "</tr>" +

            // ── Footer ────────────────────────────────────────────────────────
            "<tr>" +
            "<td style=\"padding:20px 32px;background-color:#f9fafb;border-radius:0 0 8px 8px;\">" +
              "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">" +
              "<tr>" +
                "<td>" +
                  "<p style=\"margin:0;font-size:13px;color:#374151;font-weight:bold;\">" +
                    escapeHtml(clinicName) +
                  "</p>" +
                  (clinicPhone.isBlank() ? "" :
                  "<p style=\"margin:4px 0 0;font-size:12px;color:#6b7280;\">&#128222; " + escapeHtml(clinicPhone) + "</p>") +
                "</td>" +
                "<td align=\"right\">" +
                  "<p style=\"margin:0;font-size:11px;color:#9ca3af;\">Este é um e-mail automático.</p>" +
                  "<p style=\"margin:2px 0 0;font-size:11px;color:#9ca3af;\">Por favor, não responda.</p>" +
                "</td>" +
              "</tr>" +
              "</table>" +
            "</td>" +
            "</tr>" +

            "</table>" + // fim card
            "</td></tr>" +
            "</table>" + // fim wrapper
            "</body></html>";
    }

    // -------------------------------------------------------------------------
    // Badge colorido por ação
    // -------------------------------------------------------------------------

    private String buildBadge(Action action) {
        String icon, label, bgColor, textColor, borderColor;
        switch (action) {
            case CONFIRMADO -> {
                icon = "✓"; label = "Agendamento Confirmado";
                bgColor = "#f0fdf4"; textColor = "#15803d"; borderColor = "#bbf7d0";
            }
            case ATUALIZADO -> {
                icon = "↻"; label = "Agendamento Atualizado";
                bgColor = "#eff6ff"; textColor = "#1d4ed8"; borderColor = "#bfdbfe";
            }
            default -> {
                icon = "✕"; label = "Agendamento Cancelado";
                bgColor = "#fef2f2"; textColor = "#dc2626"; borderColor = "#fecaca";
            }
        }
        return "<table cellpadding=\"0\" cellspacing=\"0\" style=\"" +
               "background-color:" + bgColor + ";" +
               "border:1px solid " + borderColor + ";" +
               "border-radius:6px;padding:0;width:100%;\">" +
               "<tr><td style=\"padding:12px 16px;\">" +
               "<span style=\"font-size:16px;font-weight:bold;color:" + textColor + ";\">" +
               icon + "&nbsp;&nbsp;" + label + "</span>" +
               "</td></tr></table>";
    }

    // -------------------------------------------------------------------------
    // Mensagem descritiva por ação
    // -------------------------------------------------------------------------

    private String buildActionMessage(Action action, String patientName, String phone) {
        String contact = phone.isBlank()
                ? "entre em contato com a clínica."
                : "entre em contato pelo telefone <strong>" + escapeHtml(phone) + "</strong>.";

        return switch (action) {
            case CONFIRMADO ->
                "Seu agendamento foi realizado com sucesso. Confira os detalhes abaixo e, " +
                "em caso de dúvidas, " + contact;
            case ATUALIZADO ->
                "As informações do seu agendamento foram atualizadas. " +
                "Confira o novo horário abaixo e, em caso de dúvidas, " + contact;
            case CANCELADO ->
                "Seu agendamento foi cancelado. Caso deseje remarcar, " + contact;
        };
    }

    // -------------------------------------------------------------------------
    // Tabela de detalhes
    // -------------------------------------------------------------------------

    private String buildDetailsTable(String data, String hora, String duracao,
                                     String profissional, Patient patient, String clinicName) {
        return "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"" +
               "border-collapse:collapse;border:1px solid #e5e7eb;border-radius:6px;font-size:14px;\">" +
               buildRow("Paciente",      escapeHtml(patient.getFirstName()), false) +
               buildRow("Data",          data,                               true) +
               buildRow("Horário",       hora,                               false) +
               buildRow("Duração",       duracao,                            true) +
               buildRow("Profissional",  escapeHtml(profissional),           false) +
               buildRow("Clínica",       escapeHtml(clinicName),             true) +
               "</table>";
    }

    private String buildRow(String label, String value, boolean shaded) {
        String bg = shaded ? "background-color:#f9fafb;" : "";
        return "<tr style=\"" + bg + "\">" +
               "<td style=\"padding:11px 16px;color:#6b7280;font-weight:600;" +
               "border-bottom:1px solid #e5e7eb;width:38%;\">" + label + "</td>" +
               "<td style=\"padding:11px 16px;color:#111827;" +
               "border-bottom:1px solid #e5e7eb;\">" + value + "</td>" +
               "</tr>";
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String getProfessionalName(Appointment appointment) {
        var user = appointment.getProfessional().getUser();
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last  = user.getLastName()  != null ? user.getLastName()  : "";
        return (first + " " + last).trim();
    }

    private String formatDuration(int minutes) {
        if (minutes < 60) return minutes + " min";
        int h = minutes / 60;
        int m = minutes % 60;
        return m == 0 ? h + "h" : h + "h " + m + "min";
    }

    private boolean hasEmail(Patient patient) {
        if (patient.getEmail() == null || patient.getEmail().isBlank()) {
            log.debug("Paciente {} sem e-mail; notificação ignorada.", patient.getId());
            return false;
        }
        return true;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    // -------------------------------------------------------------------------
    // Enum de ações
    // -------------------------------------------------------------------------

    private enum Action {
        CONFIRMADO("confirmado"),
        ATUALIZADO("atualizado"),
        CANCELADO("cancelado");

        final String tagValue;
        Action(String tagValue) { this.tagValue = tagValue; }
    }
}

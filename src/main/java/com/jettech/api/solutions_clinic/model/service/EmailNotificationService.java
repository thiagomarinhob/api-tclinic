package com.jettech.api.solutions_clinic.model.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.resend.services.emails.model.Tag;
import com.jettech.api.solutions_clinic.config.ResendConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final Resend resend;
    private final ResendConfig resendConfig;

    public Optional<String> sendEmail(String from, String to, String subject, String html) {
        return sendEmail(from, to, subject, html, List.of());
    }

    public Optional<String> sendEmail(String from, String to, String subject, String html, List<Tag> tags) {
        if (!resendConfig.isConfigured()) {
            log.warn("Resend não configurado; e-mail não enviado para {}", maskEmail(to));
            return Optional.empty();
        }

        CreateEmailOptions.Builder builder = CreateEmailOptions.builder()
                .from(from)
                .to(to)
                .subject(subject)
                .html(html);

        if (tags != null && !tags.isEmpty()) {
            builder.tags(tags);
        }

        try {
            CreateEmailResponse response = resend.emails().send(builder.build());
            log.debug("E-mail enviado via Resend, id={}", response.getId());
            return Optional.ofNullable(response.getId());
        } catch (ResendException e) {
            log.error("Erro ao enviar e-mail via Resend para {}: {}", maskEmail(to), e.getMessage());
            return Optional.empty();
        }
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        return "***" + email.substring(email.indexOf('@'));
    }
}

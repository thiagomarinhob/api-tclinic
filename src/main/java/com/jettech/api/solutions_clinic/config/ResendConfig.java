package com.jettech.api.solutions_clinic.config;

import com.resend.Resend;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Configuration
public class ResendConfig {

    @Value("${resend.api.key:}")
    private String apiKey;

    @Value("${resend.from:onboarding@resend.dev}")
    private String from;

    @Bean
    public Resend resend() {
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("re_xxxxxxxxx")) {
            log.warn("Resend API key não configurada. Envio de e-mails não funcionará.");
        } else {
            log.info("Resend API configurada com sucesso.");
        }
        return new Resend(apiKey);
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("re_xxxxxxxxx");
    }
}

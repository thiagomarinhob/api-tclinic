package com.jettech.api.solutions_clinic.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@Getter
public class WhatsAppConfig {

    @Value("${whatsapp.api.access-token:}")
    private String accessToken;

    @Value("${whatsapp.api.version:v21.0}")
    private String apiVersion;

    @Value("${whatsapp.api.phone-number-id:}")
    private String phoneNumberId;

    @PostConstruct
    public void init() {
        if (accessToken != null && !accessToken.isBlank()) {
            log.info("WhatsApp API configurada (version={}, phoneNumberId definido)", apiVersion);
        } else {
            log.warn("WhatsApp ACCESS_TOKEN não configurado. Notificações via WhatsApp não serão enviadas.");
        }
    }

    public boolean isConfigured() {
        return accessToken != null && !accessToken.isBlank()
                && phoneNumberId != null && !phoneNumberId.isBlank();
    }
}

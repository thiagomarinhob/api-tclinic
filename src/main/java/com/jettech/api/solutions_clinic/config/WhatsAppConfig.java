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

    @Value("${evolution.api.url:}")
    private String apiUrl;

    @Value("${evolution.api.key:}")
    private String apiKey;

    @Value("${evolution.instance.name:tclinic}")
    private String instanceName;

    @PostConstruct
    public void init() {
        if (isConfigured()) {
            log.info("Evolution API configurada (url={}, instance={})", apiUrl, instanceName);
        } else {
            log.warn("Evolution API não configurada. Notificações via WhatsApp não serão enviadas.");
        }
    }

    public boolean isConfigured() {
        return apiUrl != null && !apiUrl.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && instanceName != null && !instanceName.isBlank();
    }
}

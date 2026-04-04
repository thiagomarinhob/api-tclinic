package com.jettech.api.solutions_clinic.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class StripeConfig {

    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    @Value("${stripe.api.secret:}")
    private String stripeApiSecret;

    @PostConstruct
    public void init() {
        if (stripeApiSecret != null && !stripeApiSecret.isEmpty()) {
            Stripe.apiKey = stripeApiSecret;
            log.info("Stripe API configurada com sucesso");
        } else {
            log.warn("Stripe API secret não configurada. Pagamentos não funcionarão.");
        }
    }
}

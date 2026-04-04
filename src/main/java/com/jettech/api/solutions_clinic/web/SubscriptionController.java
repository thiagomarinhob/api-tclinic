package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.usecase.subscription.ProcessStripeWebhookUseCase;
import com.jettech.api.solutions_clinic.model.usecase.subscription.ProcessStripeWebhookRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/subscriptions")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SubscriptionController implements SubscriptionAPI {

    private final ProcessStripeWebhookUseCase processStripeWebhookUseCase;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @Override
    public void handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) throws AuthenticationFailedException {
        log.info("Recebendo webhook do Stripe");
        ProcessStripeWebhookRequest request = new ProcessStripeWebhookRequest(payload, signature, webhookSecret);
        processStripeWebhookUseCase.execute(request);
    }
}

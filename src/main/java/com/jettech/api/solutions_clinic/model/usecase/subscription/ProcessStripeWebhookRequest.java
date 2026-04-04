package com.jettech.api.solutions_clinic.model.usecase.subscription;

public record ProcessStripeWebhookRequest(
    String payload,
    String signature,
    String webhookSecret
) {
}

package com.jettech.api.solutions_clinic.model.usecase.subscription;

public record CreateCheckoutSessionResponse(
    String checkoutUrl,
    String sessionId
) {
}

package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.model.entity.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminSubscriptionDetailResponse(
        UUID id,
        SubscriptionStatus status,
        BigDecimal amount,
        String currency,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd,
        LocalDateTime canceledAt,
        String stripeSubscriptionId,
        String stripeCustomerId,
        String stripeCheckoutSessionId,
        LocalDateTime createdAt
) {}

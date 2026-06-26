package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.model.entity.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminSubscriptionSummary(
        SubscriptionStatus status,
        BigDecimal amount,
        String currency,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd
) {}

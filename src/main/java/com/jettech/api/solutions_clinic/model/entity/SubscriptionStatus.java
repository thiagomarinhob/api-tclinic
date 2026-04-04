package com.jettech.api.solutions_clinic.model.entity;

public enum SubscriptionStatus {
    PENDING,        // Aguardando pagamento
    ACTIVE,         // Assinatura ativa e paga
    CANCELED,       // Assinatura cancelada
    PAST_DUE,       // Pagamento atrasado
    UNPAID          // Não pago
}

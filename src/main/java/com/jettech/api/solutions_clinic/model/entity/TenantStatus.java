package com.jettech.api.solutions_clinic.model.entity;

public enum TenantStatus {
    PENDING_SETUP, // Cadastrou, mas não escolheu plano
    TRIAL,         // plano para teste
    ACTIVE,        // Plano escolhido e pago/trial
    SUSPENDED,     // Pagamento atrasado
    CANCELED       // Cancelou o serviço
}

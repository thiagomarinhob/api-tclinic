package com.jettech.api.solutions_clinic.model.entity;

/**
 * Status do exame no fluxo da clínica.
 * REQUESTED = pedido médico criado; PENDING_RESULT = aguardando resultado; COMPLETED = resultado anexado.
 */
public enum ExamStatus {
    REQUESTED,
    PENDING_RESULT,
    COMPLETED
}

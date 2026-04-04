package com.jettech.api.solutions_clinic.exception;

import org.springframework.http.HttpStatus;

/**
 * Códigos e mensagens padronizadas de erro da API.
 * Mensagens podem ser sobrescritas via i18n usando a chave {@link #getMessageKey()}.
 */
public enum ApiError {

    // --- 400 Bad Request ---
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "error.invalidRequest", "Requisição inválida."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "error.invalidRole", "Papel inválido. Papéis válidos: OWNER, ADMIN, RECEPTION, SPECIALIST, FINANCE, READONLY."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "error.validationFailed", "Erro de validação nos dados enviados."),
    CUSTOM_PLAN_NO_CHECKOUT(HttpStatus.BAD_REQUEST, "error.customPlanNoCheckout", "Plano personalizado não pode ser pago via checkout. Entre em contato com vendas."),
    PLAN_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "error.planNotSupported", "Plano não suportado."),
    INVALID_SIGNATURE(HttpStatus.BAD_REQUEST, "error.invalidSignature", "Assinatura inválida."),
    CATEGORY_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "error.categoryTypeMismatch", "O tipo da categoria não corresponde ao tipo da transação."),

    // --- 401 Unauthorized ---
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "error.authenticationFailed", "Falha de autenticação."),

    // --- 403 Forbidden ---
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "error.accessDenied", "Acesso negado a este recurso ou clínica."),

    // --- 404 Not Found ---
    NOT_FOUND(HttpStatus.NOT_FOUND, "error.notFound", "Recurso não encontrado."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "error.entityNotFound", "%s não encontrado(a) com ID: %s."),
    ENTITY_NOT_FOUND_CLINIC(HttpStatus.NOT_FOUND, "error.entityNotFoundClinic", "Clínica não identificada. Faça login novamente."),
    ENTITY_NOT_FOUND_SCHEDULE(HttpStatus.NOT_FOUND, "error.entityNotFoundSchedule", "O profissional não possui agenda cadastrada para este dia da semana."),

    // --- 409 Conflict ---
    APPOINTMENT_CONFLICT(HttpStatus.CONFLICT, "error.appointmentConflict", "Conflito de agendamento."),
    DUPLICATE_ENTITY(HttpStatus.CONFLICT, "error.duplicateEntity", "Registro duplicado."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "error.duplicateEmail", "Email já está em uso."),
    DUPLICATE_CPF(HttpStatus.CONFLICT, "error.duplicateCpf", "CPF já está cadastrado."),
    DUPLICATE_CNPJ(HttpStatus.CONFLICT, "error.duplicateCnpj", "CNPJ já está em uso."),
    DUPLICATE_SUBDOMAIN(HttpStatus.CONFLICT, "error.duplicateSubdomain", "Subdomínio já está em uso."),
    DUPLICATE_SUBSCRIPTION(HttpStatus.CONFLICT, "error.duplicateSubscription", "Já existe uma assinatura ativa para este tenant."),
    DUPLICATE_CATEGORY_NAME(HttpStatus.CONFLICT, "error.duplicateCategoryName", "Já existe uma categoria com este nome para esta clínica."),
    DUPLICATE_PROFESSIONAL(HttpStatus.CONFLICT, "error.duplicateProfessional", "Profissional já existe para este usuário e clínica."),
    DUPLICATE_SCHEDULE(HttpStatus.CONFLICT, "error.duplicateSchedule", "Já existe uma agenda cadastrada para este profissional no %s."),
    DUPLICATE_PATIENT_CPF(HttpStatus.CONFLICT, "error.duplicatePatientCpf", "Paciente já existe com este CPF nesta clínica."),
    DUPLICATE_USER_TENANT_ROLE(HttpStatus.CONFLICT, "error.duplicateUserTenantRole", "Usuário já está associado à clínica com este papel."),

    // --- 422 Unprocessable Entity ---
    INVALID_STATE(HttpStatus.UNPROCESSABLE_ENTITY, "error.invalidState", "Estado inválido para esta operação."),
    INVALID_STATE_ALREADY_ACTIVE(HttpStatus.UNPROCESSABLE_ENTITY, "error.invalidStateAlreadyActive", "A clínica já possui um plano ativo ou está em período de teste."),
    INVALID_STATE_ALREADY_TRIAL(HttpStatus.UNPROCESSABLE_ENTITY, "error.invalidStateAlreadyTrial", "A clínica já está em período de teste."),
    INVALID_STATE_APPOINTMENT_STATUS(HttpStatus.UNPROCESSABLE_ENTITY, "error.invalidStateAppointmentStatus", "Não é possível atualizar um agendamento com o status informado."),
    INVALID_STATE_PROCEDURE_INACTIVE(HttpStatus.UNPROCESSABLE_ENTITY, "error.invalidStateProcedureInactive", "O procedimento está inativo."),
    SCHEDULE_VALIDATION(HttpStatus.UNPROCESSABLE_ENTITY, "error.scheduleValidation", "Horário ou agenda inválida."),
    SCHEDULE_START_BEFORE_END(HttpStatus.UNPROCESSABLE_ENTITY, "error.scheduleStartBeforeEnd", "O horário de início deve ser anterior ao horário de término."),
    SCHEDULE_LUNCH_ORDER(HttpStatus.UNPROCESSABLE_ENTITY, "error.scheduleLunchOrder", "O horário de início do almoço deve ser anterior ao horário de término."),
    SCHEDULE_LUNCH_WITHIN_WORK(HttpStatus.UNPROCESSABLE_ENTITY, "error.scheduleLunchWithinWork", "O horário de almoço deve estar dentro do horário de trabalho."),
    SCHEDULE_OUTSIDE_WORK_HOURS(HttpStatus.UNPROCESSABLE_ENTITY, "error.scheduleOutsideWorkHours", "O horário agendado está fora do horário de trabalho do profissional."),
    SCHEDULE_IN_LUNCH_BREAK(HttpStatus.UNPROCESSABLE_ENTITY, "error.scheduleInLunchBreak", "O horário agendado está no intervalo de almoço do profissional."),
    SCHEDULE_DURATION_MULTIPLE(HttpStatus.UNPROCESSABLE_ENTITY, "error.scheduleDurationMultiple", "A duração do agendamento deve ser múltipla de %s minutos."),

    // --- 503 Service Unavailable ---
    R2_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "error.r2NotConfigured", "Upload de exames não está configurado (Cloudflare R2)."),
    WHATSAPP_SEND_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "error.whatsappSendFailed", "Falha ao enviar notificação WhatsApp. Tente novamente mais tarde."),

    // --- 500 Internal Server Error ---
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.internalServerError", "Erro interno do servidor."),
    PAYMENT_SESSION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.paymentSessionError", "Erro ao criar sessão de pagamento.");

    private final HttpStatus status;
    private final String messageKey;
    private final String defaultMessage;

    ApiError(HttpStatus status, String messageKey, String defaultMessage) {
        this.status = status;
        this.messageKey = messageKey;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    /** Texto para o campo "error" da resposta (rótulo HTTP). */
    public String getErrorLabel() {
        return status.getReasonPhrase();
    }

    /**
     * Retorna a mensagem formatada com os argumentos (usa defaultMessage como template).
     * Se não houver args ou forem vazios, retorna defaultMessage sem formatação.
     */
    public String formatMessage(Object... args) {
        if (args == null || args.length == 0) {
            return defaultMessage;
        }
        try {
            return String.format(defaultMessage, args);
        } catch (Exception e) {
            return defaultMessage;
        }
    }
}

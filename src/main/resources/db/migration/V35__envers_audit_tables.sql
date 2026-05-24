-- =====================================================
-- Tabelas de auditoria Hibernate Envers
-- Gerenciadas pelo Flyway (DDL automático desabilitado)
-- Retenção: permanente (mínimo 20 anos – CFM 1.821/07)
-- =====================================================

-- =====================================================
-- Tabela REVINFO
-- Uma linha por transação auditada; campos extras com
-- identidade do usuário, tenant e IP da requisição
-- =====================================================
CREATE TABLE revinfo (
    rev          SERIAL       NOT NULL,
    revtstmp     BIGINT       NOT NULL,
    user_id      UUID,
    user_email   VARCHAR(150),
    tenant_id    UUID,
    ip_address   VARCHAR(45),

    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

CREATE INDEX idx_revinfo_tenant  ON revinfo (tenant_id);
CREATE INDEX idx_revinfo_user    ON revinfo (user_id);
CREATE INDEX idx_revinfo_tstmp   ON revinfo (revtstmp);

-- =====================================================
-- patients_AUD
-- Histórico de todas as alterações em pacientes
-- =====================================================
CREATE TABLE patients_aud (
    id                      UUID         NOT NULL,
    rev                     INTEGER      NOT NULL,
    rev_type                SMALLINT,

    -- campos de patients (espelham a tabela original)
    tenant_id               UUID,
    first_name              VARCHAR(255),
    mother_name             VARCHAR(255),
    cpf                     VARCHAR(11),
    birth_date              VARCHAR(10),
    gender                  VARCHAR(20),
    email                   VARCHAR(255),
    phone                   VARCHAR(255),
    whatsapp                VARCHAR(255),
    address_street          VARCHAR(255),
    address_number          VARCHAR(255),
    address_complement      VARCHAR(255),
    address_neighborhood    VARCHAR(255),
    address_city            VARCHAR(255),
    address_state           VARCHAR(255),
    address_zipcode         VARCHAR(255),
    blood_type              VARCHAR(10),
    allergies               TEXT,
    health_plan             VARCHAR(255),
    guardian_name           VARCHAR(255),
    guardian_phone          VARCHAR(255),
    guardian_relationship   VARCHAR(255),
    active                  BOOLEAN,
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,

    CONSTRAINT pk_patients_aud   PRIMARY KEY (id, rev),
    CONSTRAINT fk_patients_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE INDEX idx_patients_aud_rev ON patients_aud (rev);

-- =====================================================
-- medical_records_AUD
-- Histórico de todas as alterações em prontuários
-- =====================================================
CREATE TABLE medical_records_aud (
    id             UUID     NOT NULL,
    rev            INTEGER  NOT NULL,
    rev_type       SMALLINT,

    -- campos de medical_records (espelham a tabela original)
    appointment_id UUID,
    template_id    UUID,
    content        JSONB,
    -- vital_signs excluído via @NotAudited (sem obrigação legal específica)
    signed_at      TIMESTAMP,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,

    CONSTRAINT pk_medical_records_aud     PRIMARY KEY (id, rev),
    CONSTRAINT fk_medical_records_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE INDEX idx_medical_records_aud_rev           ON medical_records_aud (rev);
CREATE INDEX idx_medical_records_aud_appointment   ON medical_records_aud (appointment_id);

-- =====================================================
-- appointments_AUD
-- =====================================================
CREATE TABLE appointments_aud (
    id                      UUID         NOT NULL,
    rev                     INTEGER      NOT NULL,
    rev_type                SMALLINT,

    tenant_id               UUID,
    patient_id              UUID,
    professional_id         UUID,
    room_id                 UUID,
    scheduled_at            TIMESTAMP,
    duration_minutes        INTEGER,
    status                  VARCHAR(20),
    observations            TEXT,
    cancelled_at            TIMESTAMP,
    reminder_sent_at        TIMESTAMP,
    whatsapp_message_id     VARCHAR(255),
    started_at              TIMESTAMP,
    finished_at             TIMESTAMP,
    duration_actual_minutes INTEGER,
    total_value             DECIMAL(10,2),
    payment_method          VARCHAR(20),
    payment_status          VARCHAR(20),
    paid_at                 TIMESTAMP,
    created_by              UUID,
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,

    CONSTRAINT pk_appointments_aud   PRIMARY KEY (id, rev),
    CONSTRAINT fk_appointments_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE INDEX idx_appointments_aud_rev ON appointments_aud (rev);

-- =====================================================
-- patient_consents_AUD
-- =====================================================
CREATE TABLE patient_consents_aud (
    id           UUID     NOT NULL,
    rev          INTEGER  NOT NULL,
    rev_type     SMALLINT,

    patient_id   UUID,
    consent_type VARCHAR(50),
    granted      BOOLEAN,
    granted_at   TIMESTAMP,
    revoked_at   TIMESTAMP,
    ip_address   VARCHAR(45),
    term_version VARCHAR(10),

    CONSTRAINT pk_patient_consents_aud     PRIMARY KEY (id, rev),
    CONSTRAINT fk_patient_consents_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE INDEX idx_patient_consents_aud_rev ON patient_consents_aud (rev);

-- =====================================================
-- professionals_AUD
-- =====================================================
CREATE TABLE professionals_aud (
    id              UUID         NOT NULL,
    rev             INTEGER      NOT NULL,
    rev_type        SMALLINT,

    tenant_id       UUID,
    user_id         UUID,
    specialty       VARCHAR(255),
    document_type   VARCHAR(10),
    document_number VARCHAR(255),
    document_state  VARCHAR(255),
    bio             TEXT,
    active          BOOLEAN,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,

    CONSTRAINT pk_professionals_aud     PRIMARY KEY (id, rev),
    CONSTRAINT fk_professionals_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE INDEX idx_professionals_aud_rev ON professionals_aud (rev);

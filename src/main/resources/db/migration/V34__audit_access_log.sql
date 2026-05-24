CREATE TABLE audit_access_logs (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id   UUID        NOT NULL,
    user_id     UUID,
    user_email  VARCHAR(150),
    action      VARCHAR(30) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id   UUID        NOT NULL,
    patient_id  UUID,
    ip_address  VARCHAR(45),
    accessed_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_audit_access_logs PRIMARY KEY (id)
);

CREATE INDEX idx_access_log_patient ON audit_access_logs (patient_id, accessed_at DESC);
CREATE INDEX idx_access_log_user    ON audit_access_logs (user_id, accessed_at DESC);
CREATE INDEX idx_access_log_entity  ON audit_access_logs (entity_type, entity_id, accessed_at DESC);

CREATE TABLE appointment_attachments (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id  UUID         NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    tenant_id       UUID         NOT NULL REFERENCES tenant(id)       ON DELETE CASCADE,
    file_name       VARCHAR(255) NOT NULL,
    object_key      VARCHAR(512) NOT NULL,
    file_type       VARCHAR(100),
    file_size_bytes BIGINT,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_appointment_attachments_appointment_id ON appointment_attachments(appointment_id);
CREATE INDEX idx_appointment_attachments_tenant_id      ON appointment_attachments(tenant_id);

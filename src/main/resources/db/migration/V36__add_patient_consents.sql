CREATE TABLE patient_consents (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id   UUID        NOT NULL REFERENCES patients(id),
    consent_type VARCHAR(50) NOT NULL,
    granted      BOOLEAN     NOT NULL,
    granted_at   TIMESTAMP,
    revoked_at   TIMESTAMP,
    ip_address   VARCHAR(45),
    term_version VARCHAR(10)
);

CREATE INDEX idx_patient_consents_patient_id ON patient_consents(patient_id);

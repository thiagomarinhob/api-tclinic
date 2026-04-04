-- =====================================================
-- Tabela: exams
-- Pedidos de exame e resultados (multitenancy, paciente, opcional consulta)
-- =====================================================
CREATE TABLE exams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    appointment_id UUID,
    name VARCHAR(255) NOT NULL,
    clinical_indication TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'REQUESTED',
    result_file_key VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_exams_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_exams_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_exams_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL,
    CONSTRAINT chk_exam_status CHECK (status IN ('REQUESTED', 'PENDING_RESULT', 'COMPLETED'))
);

CREATE INDEX idx_exams_tenant_id ON exams(tenant_id);
CREATE INDEX idx_exams_patient_id ON exams(patient_id);
CREATE INDEX idx_exams_appointment_id ON exams(appointment_id);
CREATE INDEX idx_exams_status ON exams(status);

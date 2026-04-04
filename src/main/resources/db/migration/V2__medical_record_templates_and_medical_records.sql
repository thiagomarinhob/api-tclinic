-- =====================================================
-- Tabelas: medical_record_templates e medical_records
-- Estratégia JSONB para suportar múltiplas especialidades
-- =====================================================

-- Tabela para definir os modelos de prontuário (ex: Avaliação Fisioterápica)
CREATE TABLE medical_record_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    professional_type VARCHAR(50),
    schema JSONB NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_medical_record_templates_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
);

CREATE INDEX idx_medical_record_templates_tenant_id ON medical_record_templates(tenant_id);
CREATE INDEX idx_medical_record_templates_professional_type ON medical_record_templates(professional_type);
CREATE INDEX idx_medical_record_templates_active ON medical_record_templates(active);

-- Tabela de prontuários (respostas preenchidas conforme o template)
CREATE TABLE medical_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID NOT NULL,
    template_id UUID NOT NULL,
    content JSONB NOT NULL,
    vital_signs JSONB,
    signed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_medical_records_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT fk_medical_records_template FOREIGN KEY (template_id) REFERENCES medical_record_templates(id) ON DELETE RESTRICT,
    CONSTRAINT uk_medical_record_appointment UNIQUE (appointment_id)
);

CREATE INDEX idx_medical_records_appointment_id ON medical_records(appointment_id);
CREATE INDEX idx_medical_records_template_id ON medical_records(template_id);
CREATE INDEX idx_medical_records_signed_at ON medical_records(signed_at);

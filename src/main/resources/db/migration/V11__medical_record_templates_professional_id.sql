-- =====================================================
-- Templates por profissional: clínica (professional_id NULL) ou do profissional
-- =====================================================

ALTER TABLE medical_record_templates
    ADD COLUMN IF NOT EXISTS professional_id UUID NULL,
    ADD CONSTRAINT fk_medical_record_templates_professional
        FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_medical_record_templates_professional_id
    ON medical_record_templates(professional_id);

COMMENT ON COLUMN medical_record_templates.professional_id IS 'NULL = modelo da clínica (todos). Preenchido = modelo exclusivo do profissional.';

-- =====================================================
-- Modelo de prontuário padrão por tenant (pré-seleção no formulário)
-- =====================================================

ALTER TABLE medical_record_templates
    ADD COLUMN IF NOT EXISTS is_default boolean NOT NULL DEFAULT false;

COMMENT ON COLUMN medical_record_templates.is_default IS 'Apenas um por tenant pode ser true; usado como pré-seleção ao abrir novo prontuário.';

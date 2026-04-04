-- =====================================================
-- Padrão de modelo de prontuário por tenant (permite global ou da clínica)
-- =====================================================

ALTER TABLE tenant
    ADD COLUMN IF NOT EXISTS default_medical_record_template_id UUID NULL,
    ADD CONSTRAINT fk_tenant_default_medical_record_template
        FOREIGN KEY (default_medical_record_template_id) REFERENCES medical_record_templates(id) ON DELETE SET NULL;

COMMENT ON COLUMN tenant.default_medical_record_template_id IS 'Modelo pré-selecionado ao abrir novo prontuário; pode ser template global ou da clínica.';

-- Migrar: clínicas que tinham is_default=true em algum template passam a ter esse ID no tenant
UPDATE tenant t
SET default_medical_record_template_id = (
    SELECT m.id FROM medical_record_templates m
    WHERE m.tenant_id = t.id AND m.is_default = true
    LIMIT 1
)
WHERE EXISTS (
    SELECT 1 FROM medical_record_templates m
    WHERE m.tenant_id = t.id AND m.is_default = true
);

-- Remover coluna is_default dos templates (fonte da verdade passa a ser tenant)
ALTER TABLE medical_record_templates
    DROP COLUMN IF EXISTS is_default;

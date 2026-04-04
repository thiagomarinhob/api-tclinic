-- =====================================================
-- Escopo de visibilidade: templates globais + por tenant
-- tenant_id NULL = Template Global (disponível para todas as clínicas)
-- tenant_id NOT NULL = Template da clínica
-- =====================================================

-- Permitir tenant_id NULL (templates globais do sistema)
ALTER TABLE medical_record_templates
    ALTER COLUMN tenant_id DROP NOT NULL;

-- Coluna opcional: impede que a clínica apague/edite template padrão
ALTER TABLE medical_record_templates
    ADD COLUMN IF NOT EXISTS is_readonly BOOLEAN NOT NULL DEFAULT FALSE;

-- FK continua válida: NULL não referencia tenant; valores preenchidos referenciam tenant(id)
-- ON DELETE CASCADE só se aplica quando tenant_id não é NULL

COMMENT ON COLUMN medical_record_templates.tenant_id IS 'NULL = template global (sistema). Preenchido = template da clínica.';
COMMENT ON COLUMN medical_record_templates.is_readonly IS 'TRUE = template padrão do sistema, não deve ser apagado/editado pela clínica.';

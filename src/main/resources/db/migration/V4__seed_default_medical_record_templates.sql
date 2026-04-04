-- =====================================================
-- Template padrão de prontuário (global, disponível para todas as clínicas)
-- tenant_id NULL = template do sistema
-- is_readonly TRUE = não pode ser apagado/editado pela clínica
-- =====================================================

INSERT INTO medical_record_templates (
    id,
    tenant_id,
    name,
    professional_type,
    schema,
    is_readonly,
    active,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    NULL,
    'Prontuário Padrão - Consulta Médica',
    NULL,
    '[
        {"id": "queixa_principal", "label": "Queixa Principal", "type": "textarea", "placeholder": "Descreva a queixa principal do paciente..."},
        {"id": "historia_doenca_atual", "label": "História da Doença Atual", "type": "textarea", "placeholder": "Descreva a evolução dos sintomas, fatores de melhora/piora..."},
        {"id": "exame_fisico", "label": "Exame Físico", "type": "textarea", "placeholder": "Registre os achados do exame físico..."},
        {"id": "hipotese_diagnostica", "label": "Hipótese Diagnóstica", "type": "textarea", "placeholder": "Descreva as hipóteses diagnósticas..."},
        {"id": "plano_tratamento", "label": "Plano de Tratamento", "type": "textarea", "placeholder": "Descreva o plano terapêutico..."},
        {"id": "prescricoes", "label": "Prescrições", "type": "textarea", "placeholder": "Liste os medicamentos prescritos..."},
        {"id": "observacoes", "label": "Observações", "type": "textarea", "placeholder": "Observações gerais..."}
    ]'::jsonb,
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

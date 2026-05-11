-- =====================================================
-- V31: Adiciona campos "Prescrições" e "Solicitação de
--      Encaminhamento" ao template Prontuário Odontológico.
-- =====================================================

UPDATE medical_record_templates
SET schema = schema || '[
    {"id": "prescricoes",                "label": "Prescrições",                   "type": "prescricao_comum", "order": 9},
    {"id": "solicitacao_encaminhamento", "label": "Solicitação de Encaminhamento", "type": "textarea",         "placeholder": "Descreva o encaminhamento solicitado...", "order": 10}
]'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE name = 'Prontuário Odontológico'
  AND tenant_id IS NULL;

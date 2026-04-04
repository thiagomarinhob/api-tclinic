-- =====================================================
-- Adiciona campos de documentos ao template padrão global:
--   Solicitação de Exames, Solicitação de Encaminhamento, Atestado
-- Também atribui order explícito a todos os campos para ordenação estável.
-- =====================================================

UPDATE medical_record_templates
SET schema = '[
    {"id": "queixa_principal",         "label": "Queixa Principal",              "type": "textarea", "placeholder": "Descreva a queixa principal do paciente...",                    "order": 1},
    {"id": "historia_doenca_atual",    "label": "História da Doença Atual",       "type": "textarea", "placeholder": "Descreva a evolução dos sintomas, fatores de melhora/piora...", "order": 2},
    {"id": "exame_fisico",             "label": "Exame Físico",                   "type": "textarea", "placeholder": "Registre os achados do exame físico...",                       "order": 3},
    {"id": "hipotese_diagnostica",     "label": "Hipótese Diagnóstica",           "type": "textarea", "placeholder": "Descreva as hipóteses diagnósticas...",                        "order": 4},
    {"id": "plano_tratamento",         "label": "Plano de Tratamento",            "type": "textarea", "placeholder": "Descreva o plano terapêutico...",                              "order": 5},
    {"id": "prescricoes",              "label": "Prescrições",                    "type": "textarea", "placeholder": "Liste os medicamentos prescritos...",                          "order": 6},
    {"id": "observacoes",              "label": "Observações",                    "type": "textarea", "placeholder": "Observações gerais...",                                        "order": 7},
    {"id": "solicitacao_exames",       "label": "Solicitação de Exames",          "type": "textarea", "placeholder": "Descreva os exames solicitados...",                            "order": 8},
    {"id": "solicitacao_encaminhamento","label": "Solicitação de Encaminhamento", "type": "textarea", "placeholder": "Descreva o encaminhamento solicitado...",                      "order": 9},
    {"id": "atestado",                 "label": "Atestado",                       "type": "textarea", "placeholder": "Informe os dados do atestado médico...",                       "order": 10}
]'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE name = 'Prontuário Padrão - Consulta Médica'
  AND tenant_id IS NULL;

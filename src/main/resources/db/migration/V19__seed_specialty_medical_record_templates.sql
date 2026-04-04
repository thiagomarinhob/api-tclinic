-- =====================================================
-- Templates de prontuário por especialidade (globais, somente leitura)
-- tenant_id NULL = template do sistema
-- professional_type = valor do enum Specialty
-- =====================================================

-- PSICOLOGISTA
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Psicológico', 'PSICOLOGISTA',
    '[
        {"id": "demanda_apresentada",        "label": "Demanda Apresentada",                "type": "textarea", "placeholder": "Descreva o motivo da consulta e a queixa principal...",                                "order": 1},
        {"id": "historia_pessoal_familiar",  "label": "História Pessoal e Familiar",        "type": "textarea", "placeholder": "Aspectos relevantes da história de vida, família e contexto social...",             "order": 2},
        {"id": "saude_mental_atual",         "label": "Saúde Mental Atual",                 "type": "textarea", "placeholder": "Humor, comportamento, sintomas e funcionamento geral...",                          "order": 3},
        {"id": "hipotese_diagnostica",       "label": "Hipótese Diagnóstica (CID/DSM)",     "type": "textarea", "placeholder": "Hipóteses com base nos critérios CID-11 ou DSM-5...",                            "order": 4},
        {"id": "plano_terapeutico",          "label": "Plano Terapêutico",                  "type": "textarea", "placeholder": "Objetivos, técnicas e abordagem terapêutica...",                                  "order": 5},
        {"id": "evolucao_sessao",            "label": "Evolução da Sessão",                 "type": "textarea", "placeholder": "Resumo do que foi trabalhado na sessão...",                                       "order": 6},
        {"id": "observacoes",                "label": "Observações",                        "type": "textarea", "placeholder": "Observações adicionais...",                                                        "order": 7},
        {"id": "solicitacao_encaminhamento", "label": "Solicitação de Encaminhamento",      "type": "textarea", "placeholder": "Descreva o encaminhamento solicitado...",                                         "order": 8},
        {"id": "atestado",                   "label": "Atestado",                           "type": "textarea", "placeholder": "Informe os dados do atestado...",                                                  "order": 9}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- PSIQUIATRA
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Psiquiátrico', 'PSIQUIATRA',
    '[
        {"id": "queixa_principal",              "label": "Queixa Principal",                    "type": "textarea", "placeholder": "Descreva a queixa principal do paciente...",                                   "order": 1},
        {"id": "historia_psiquiatrica",         "label": "História Psiquiátrica",               "type": "textarea", "placeholder": "Episódios anteriores, internações, tratamentos prévios...",                  "order": 2},
        {"id": "historico_familiar",            "label": "Histórico Familiar Psiquiátrico",     "type": "textarea", "placeholder": "Transtornos mentais ou uso de substâncias na família...",                    "order": 3},
        {"id": "exame_estado_mental",           "label": "Exame do Estado Mental",              "type": "textarea", "placeholder": "Aparência, humor, afeto, pensamento, percepção, cognição, juízo crítico...", "order": 4},
        {"id": "hipotese_diagnostica",          "label": "Hipótese Diagnóstica (CID/DSM)",      "type": "textarea", "placeholder": "Hipóteses com base nos critérios CID-11 ou DSM-5...",                       "order": 5},
        {"id": "plano_tratamento",              "label": "Plano de Tratamento",                 "type": "textarea", "placeholder": "Estratégia terapêutica, metas e próximos passos...",                         "order": 6},
        {"id": "prescricoes",                   "label": "Prescrições",                         "type": "textarea", "placeholder": "Liste os medicamentos prescritos com dose e posologia...",                   "order": 7},
        {"id": "observacoes",                   "label": "Observações",                         "type": "textarea", "placeholder": "Observações adicionais...",                                                   "order": 8},
        {"id": "solicitacao_exames",            "label": "Solicitação de Exames",               "type": "textarea", "placeholder": "Descreva os exames solicitados...",                                          "order": 9},
        {"id": "atestado",                      "label": "Atestado",                            "type": "textarea", "placeholder": "Informe os dados do atestado...",                                             "order": 10}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- FISIOTERAPEUTA
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Fisioterapêutico', 'FISIOTERAPEUTA',
    '[
        {"id": "queixa_principal",              "label": "Queixa Principal",                    "type": "textarea", "placeholder": "Descreva a queixa principal e localização da dor/limitação...",               "order": 1},
        {"id": "historia_clinica",              "label": "História Clínica e Funcional",         "type": "textarea", "placeholder": "Início dos sintomas, fatores agravantes/aliviantes, tratamentos anteriores...", "order": 2},
        {"id": "avaliacao_funcional",           "label": "Avaliação Postural/Funcional",         "type": "textarea", "placeholder": "Postura, amplitude de movimento, força muscular, marcha...",                  "order": 3},
        {"id": "testes_especificos",            "label": "Testes Específicos",                   "type": "textarea", "placeholder": "Resultados dos testes ortopédicos e funcionais realizados...",                "order": 4},
        {"id": "diagnostico_fisioterapeutico",  "label": "Diagnóstico Fisioterapêutico",         "type": "textarea", "placeholder": "Diagnóstico funcional baseado na avaliação...",                              "order": 5},
        {"id": "objetivos_tratamento",          "label": "Objetivos do Tratamento",              "type": "textarea", "placeholder": "Metas de curto e longo prazo...",                                            "order": 6},
        {"id": "plano_tratamento",              "label": "Plano de Tratamento",                  "type": "textarea", "placeholder": "Recursos e técnicas fisioterapêuticas a serem utilizados...",                "order": 7},
        {"id": "evolucao_sessao",               "label": "Evolução da Sessão",                   "type": "textarea", "placeholder": "Procedimentos realizados e resposta do paciente...",                         "order": 8},
        {"id": "solicitacao_encaminhamento",    "label": "Solicitação de Encaminhamento",        "type": "textarea", "placeholder": "Descreva o encaminhamento solicitado...",                                     "order": 9}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- DENTISTA
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Odontológico', 'DENTISTA',
    '[
        {"id": "queixa_principal",          "label": "Queixa Principal",                    "type": "textarea", "placeholder": "Descreva a queixa ou motivo da consulta...",                                   "order": 1},
        {"id": "historico_odontologico",    "label": "Histórico Odontológico",              "type": "textarea", "placeholder": "Tratamentos anteriores, alergias a anestésicos, medicamentos em uso...",      "order": 2},
        {"id": "exame_intraoral",           "label": "Exame Intraoral / Periodontal",       "type": "textarea", "placeholder": "Condições dos dentes, gengiva, mucosa, oclusão...",                          "order": 3},
        {"id": "procedimento_realizado",    "label": "Procedimento Realizado",              "type": "textarea", "placeholder": "Descreva o procedimento executado nesta consulta...",                         "order": 4},
        {"id": "materiais_utilizados",      "label": "Materiais Utilizados",                "type": "textarea", "placeholder": "Materiais, marcas e especificações utilizados...",                            "order": 5},
        {"id": "plano_tratamento",          "label": "Plano de Tratamento",                 "type": "textarea", "placeholder": "Próximas etapas do tratamento...",                                            "order": 6},
        {"id": "observacoes",               "label": "Observações",                         "type": "textarea", "placeholder": "Observações adicionais, intercorrências...",                                  "order": 7},
        {"id": "solicitacao_exames",        "label": "Solicitação de Exames",               "type": "textarea", "placeholder": "Radiografias, tomografias ou outros exames solicitados...",                   "order": 8}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- CARDIOLOGISTA
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Cardiológico', 'CARDIOLOGISTA',
    '[
        {"id": "queixa_principal",              "label": "Queixa Principal",                    "type": "textarea", "placeholder": "Descreva a queixa principal do paciente...",                                   "order": 1},
        {"id": "historia_doenca_atual",         "label": "História da Doença Atual",            "type": "textarea", "placeholder": "Evolução dos sintomas, localização, irradiação, intensidade...",              "order": 2},
        {"id": "fatores_risco",                 "label": "Fatores de Risco Cardiovascular",     "type": "textarea", "placeholder": "HAS, DM, dislipidemia, tabagismo, obesidade, sedentarismo, histórico familiar...", "order": 3},
        {"id": "exame_fisico",                  "label": "Exame Físico",                        "type": "textarea", "placeholder": "PA, FC, FR, SpO2, ausculta cardíaca e pulmonar, edemas...",                   "order": 4},
        {"id": "ecg_exames",                    "label": "ECG / Exames Complementares",         "type": "textarea", "placeholder": "Achados do ECG e resultados de exames...",                                    "order": 5},
        {"id": "hipotese_diagnostica",          "label": "Hipótese Diagnóstica",                "type": "textarea", "placeholder": "Descreva as hipóteses diagnósticas...",                                       "order": 6},
        {"id": "plano_tratamento",              "label": "Plano de Tratamento",                 "type": "textarea", "placeholder": "Conduta terapêutica, mudanças de estilo de vida, medicamentos...",            "order": 7},
        {"id": "prescricoes",                   "label": "Prescrições",                         "type": "textarea", "placeholder": "Liste os medicamentos prescritos com dose e posologia...",                   "order": 8},
        {"id": "observacoes",                   "label": "Observações",                         "type": "textarea", "placeholder": "Observações adicionais...",                                                   "order": 9},
        {"id": "solicitacao_exames",            "label": "Solicitação de Exames",               "type": "textarea", "placeholder": "Exames laboratoriais, ecocardiograma, teste ergométrico, holter...",         "order": 10},
        {"id": "solicitacao_encaminhamento",    "label": "Solicitação de Encaminhamento",       "type": "textarea", "placeholder": "Descreva o encaminhamento solicitado...",                                     "order": 11},
        {"id": "atestado",                      "label": "Atestado",                            "type": "textarea", "placeholder": "Informe os dados do atestado...",                                             "order": 12}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- PEDIATRA
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Pediátrico', 'PEDIATRA',
    '[
        {"id": "motivo_consulta",               "label": "Motivo da Consulta",                  "type": "textarea", "placeholder": "Queixa principal e motivo da visita...",                                       "order": 1},
        {"id": "anamnese_pediatrica",           "label": "Anamnese Pediátrica",                 "type": "textarea", "placeholder": "Gestação, parto, alimentação, vacinas, doenças pregressas, alergias...",     "order": 2},
        {"id": "desenvolvimento",               "label": "Desenvolvimento Neuropsicomotor",      "type": "textarea", "placeholder": "Marcos do desenvolvimento: motor, linguagem, social e cognitivo...",          "order": 3},
        {"id": "exame_fisico",                  "label": "Exame Físico",                        "type": "textarea", "placeholder": "Peso, altura, PC, PA, FC, FR, temperatura, achados gerais...",               "order": 4},
        {"id": "hipotese_diagnostica",          "label": "Hipótese Diagnóstica",                "type": "textarea", "placeholder": "Descreva as hipóteses diagnósticas...",                                       "order": 5},
        {"id": "plano_tratamento",              "label": "Plano de Tratamento",                 "type": "textarea", "placeholder": "Conduta terapêutica e orientações...",                                        "order": 6},
        {"id": "prescricoes",                   "label": "Prescrições",                         "type": "textarea", "placeholder": "Medicamentos prescritos com dose por kg e posologia...",                     "order": 7},
        {"id": "orientacoes_responsaveis",      "label": "Orientações aos Responsáveis",        "type": "textarea", "placeholder": "Cuidados em casa, sinais de alerta, retorno...",                              "order": 8},
        {"id": "solicitacao_exames",            "label": "Solicitação de Exames",               "type": "textarea", "placeholder": "Descreva os exames solicitados...",                                           "order": 9},
        {"id": "atestado",                      "label": "Atestado",                            "type": "textarea", "placeholder": "Informe os dados do atestado...",                                             "order": 10}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- =====================================================
-- Templates de prontuário para especialidades restantes (globais, somente leitura)
-- tenant_id NULL = template do sistema
-- professional_type = valor do enum Specialty
-- =====================================================

-- CLINICO_GERAL
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Clínico Geral', 'CLINICO_GERAL',
    '[
        {"id": "queixa_principal",           "label": "Queixa Principal",                  "type": "textarea", "placeholder": "Descreva a queixa principal do paciente...",                                     "order": 1},
        {"id": "historia_doenca_atual",      "label": "História da Doença Atual",           "type": "textarea", "placeholder": "Evolução dos sintomas, início, intensidade, fatores de melhora/piora...",      "order": 2},
        {"id": "antecedentes_pessoais",      "label": "Antecedentes Pessoais",              "type": "textarea", "placeholder": "Doenças crônicas, cirurgias anteriores, alergias, vacinação...",               "order": 3},
        {"id": "medicamentos_em_uso",        "label": "Medicamentos em Uso",                "type": "textarea", "placeholder": "Liste os medicamentos em uso com dose e frequência...",                        "order": 4},
        {"id": "exame_fisico",               "label": "Exame Físico",                       "type": "textarea", "placeholder": "PA, FC, FR, Temperatura, SpO2, peso, altura, achados relevantes...",          "order": 5},
        {"id": "hipotese_diagnostica",       "label": "Hipótese Diagnóstica",               "type": "textarea", "placeholder": "Descreva as hipóteses diagnósticas...",                                       "order": 6},
        {"id": "plano_tratamento",           "label": "Plano de Tratamento",                "type": "textarea", "placeholder": "Conduta terapêutica, orientações e medidas não-farmacológicas...",            "order": 7},
        {"id": "prescricoes",                "label": "Prescrições",                        "type": "textarea", "placeholder": "Liste os medicamentos prescritos com dose e posologia...",                   "order": 8},
        {"id": "observacoes",                "label": "Observações",                        "type": "textarea", "placeholder": "Observações adicionais...",                                                   "order": 9},
        {"id": "solicitacao_exames",         "label": "Solicitação de Exames",              "type": "textarea", "placeholder": "Exames laboratoriais e de imagem solicitados...",                             "order": 10},
        {"id": "solicitacao_encaminhamento", "label": "Solicitação de Encaminhamento",      "type": "textarea", "placeholder": "Descreva o encaminhamento solicitado...",                                     "order": 11},
        {"id": "atestado",                   "label": "Atestado",                           "type": "textarea", "placeholder": "Informe os dados do atestado...",                                             "order": 12}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- DERMATOLOGISTA
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Dermatológico', 'DERMATOLOGISTA',
    '[
        {"id": "queixa_principal",           "label": "Queixa Principal",                  "type": "textarea", "placeholder": "Lesão, prurido, descamação, alteração de pele ou unhas...",                    "order": 1},
        {"id": "historia_doenca_atual",      "label": "História da Doença Atual",           "type": "textarea", "placeholder": "Início, evolução, fatores de piora/melhora, tratamentos usados...",           "order": 2},
        {"id": "antecedentes_dermatologicos","label": "Antecedentes Dermatológicos",        "type": "textarea", "placeholder": "Alergias, doenças prévias de pele, histórico familiar, atopia...",            "order": 3},
        {"id": "medicamentos_uso_topico",    "label": "Medicamentos em Uso (sistêmico/tópico)", "type": "textarea", "placeholder": "Medicamentos sistêmicos e tópicos em uso...",                           "order": 4},
        {"id": "descricao_lesoes",           "label": "Descrição das Lesões",               "type": "textarea", "placeholder": "Localização, morfologia (mácula, pápula, placa...), cor, bordas, tamanho, distribuição...", "order": 5},
        {"id": "fototipo_solar",             "label": "Fototipo / Exposição Solar",         "type": "textarea", "placeholder": "Fototipo de Fitzpatrick, histórico de exposição solar e uso de protetor...",  "order": 6},
        {"id": "hipotese_diagnostica",       "label": "Hipótese Diagnóstica",               "type": "textarea", "placeholder": "Descreva as hipóteses diagnósticas...",                                       "order": 7},
        {"id": "conduta_tratamento",         "label": "Conduta / Tratamento",               "type": "textarea", "placeholder": "Tratamento proposto (tópico, sistêmico, procedimento)...",                   "order": 8},
        {"id": "orientacoes_paciente",       "label": "Orientações ao Paciente",            "type": "textarea", "placeholder": "Cuidados com a pele, proteção solar, sinais de alerta...",                   "order": 9},
        {"id": "solicitacao_exames",         "label": "Solicitação de Exames",              "type": "textarea", "placeholder": "Biópsia, cultura, dermatoscopia, exames laboratoriais...",                    "order": 10},
        {"id": "atestado",                   "label": "Atestado",                           "type": "textarea", "placeholder": "Informe os dados do atestado...",                                             "order": 11}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- ENDOCRINOLOGISTA
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Endocrinológico', 'ENDOCRINOLOGISTA',
    '[
        {"id": "queixa_principal",           "label": "Queixa Principal",                  "type": "textarea", "placeholder": "Descreva a queixa principal do paciente...",                                     "order": 1},
        {"id": "historia_doenca_atual",      "label": "História da Doença Atual",           "type": "textarea", "placeholder": "Evolução dos sintomas, controle metabólico atual...",                         "order": 2},
        {"id": "antecedentes_metabolicos",   "label": "Antecedentes Pessoais",              "type": "textarea", "placeholder": "DM, tireoidopatia, obesidade, dislipidemia, síndrome metabólica, osteoporose...", "order": 3},
        {"id": "historico_familiar",         "label": "Histórico Familiar",                 "type": "textarea", "placeholder": "Diabetes, obesidade, doenças tireoidianas, neoplasias endócrinas...",          "order": 4},
        {"id": "medicamentos_em_uso",        "label": "Medicamentos em Uso",                "type": "textarea", "placeholder": "Liste os medicamentos em uso com dose e frequência...",                        "order": 5},
        {"id": "exame_fisico",               "label": "Exame Físico",                       "type": "textarea", "placeholder": "Peso, altura, IMC, CA, PA, exame da tireoide, acantose, estrias...",          "order": 6},
        {"id": "resultados_laboratoriais",   "label": "Resultados de Exames",               "type": "textarea", "placeholder": "Glicemia, HbA1c, TSH, T4L, lipidograma, cortisol, insulina, vitamina D...",   "order": 7},
        {"id": "hipotese_diagnostica",       "label": "Hipótese Diagnóstica",               "type": "textarea", "placeholder": "Descreva as hipóteses diagnósticas...",                                       "order": 8},
        {"id": "plano_tratamento",           "label": "Plano de Tratamento",                "type": "textarea", "placeholder": "Conduta farmacológica e mudanças de estilo de vida...",                       "order": 9},
        {"id": "metas_terapeuticas",         "label": "Metas Terapêuticas",                 "type": "textarea", "placeholder": "Metas glicêmicas, lipídicas, de peso, pressóricas...",                       "order": 10},
        {"id": "prescricoes",                "label": "Prescrições",                        "type": "textarea", "placeholder": "Liste os medicamentos prescritos com dose e posologia...",                   "order": 11},
        {"id": "solicitacao_exames",         "label": "Solicitação de Exames",              "type": "textarea", "placeholder": "Exames laboratoriais, densitometria, cintilografia, ultrassom...",            "order": 12},
        {"id": "solicitacao_encaminhamento", "label": "Solicitação de Encaminhamento",      "type": "textarea", "placeholder": "Descreva o encaminhamento solicitado...",                                     "order": 13},
        {"id": "atestado",                   "label": "Atestado",                           "type": "textarea", "placeholder": "Informe os dados do atestado...",                                             "order": 14}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- ENFERMEIRO (baseado na SAE - Sistematização da Assistência de Enfermagem)
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário de Enfermagem (SAE)', 'ENFERMEIRO',
    '[
        {"id": "historico_enfermagem",       "label": "Histórico de Enfermagem",            "type": "textarea", "placeholder": "Coleta de dados: queixa, antecedentes, hábitos, condições de saúde...",        "order": 1},
        {"id": "diagnostico_enfermagem",     "label": "Diagnóstico de Enfermagem (NANDA)",  "type": "textarea", "placeholder": "Problemas e necessidades identificadas conforme taxonomia NANDA-I...",         "order": 2},
        {"id": "prescricao_enfermagem",      "label": "Prescrição de Enfermagem",           "type": "textarea", "placeholder": "Intervenções e cuidados planejados para atender às necessidades identificadas...", "order": 3},
        {"id": "implementacao",              "label": "Implementação / Procedimentos",       "type": "textarea", "placeholder": "Procedimentos realizados: curativos, medicações, sondagens, punções...",        "order": 4},
        {"id": "evolucao_enfermagem",        "label": "Evolução de Enfermagem",             "type": "textarea", "placeholder": "Resposta do paciente às intervenções, evolução do quadro clínico...",           "order": 5},
        {"id": "sinais_vitais_adicionais",   "label": "Sinais Vitais / Dados Clínicos",     "type": "textarea", "placeholder": "Diurese, balanço hídrico, saturação, glicemia capilar, escala de dor...",      "order": 6},
        {"id": "orientacoes_paciente",       "label": "Orientações ao Paciente / Família",  "type": "textarea", "placeholder": "Orientações sobre cuidados, medicamentos, sinais de alerta e retorno...",      "order": 7},
        {"id": "observacoes",                "label": "Observações",                        "type": "textarea", "placeholder": "Observações adicionais...",                                                   "order": 8}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- GASTROENTEROLOGISTA
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Gastroenterológico', 'GASTROENTEROLOGISTA',
    '[
        {"id": "queixa_principal",           "label": "Queixa Principal",                  "type": "textarea", "placeholder": "Descreva a queixa principal (dor abdominal, náusea, diarreia, constipação...)...", "order": 1},
        {"id": "historia_doenca_atual",      "label": "História da Doença Atual",           "type": "textarea", "placeholder": "Evolução dos sintomas, relação com alimentação, fatores de piora/melhora...",  "order": 2},
        {"id": "habitos_intestinais",        "label": "Hábitos Intestinais e Alimentares",  "type": "textarea", "placeholder": "Frequência, consistência, sangue/muco nas fezes, flatulência, hábito alimentar...", "order": 3},
        {"id": "antecedentes_digestivos",    "label": "Antecedentes Digestivos",            "type": "textarea", "placeholder": "Gastrite, úlcera, hepatite, pancreatite, doença inflamatória, cirurgias abdominais...", "order": 4},
        {"id": "medicamentos_em_uso",        "label": "Medicamentos em Uso",                "type": "textarea", "placeholder": "Especialmente IBPs, AINEs, antibióticos, laxantes...",                          "order": 5},
        {"id": "exame_fisico_abdominal",     "label": "Exame Físico Abdominal",             "type": "textarea", "placeholder": "Inspeção, ausculta, palpação (dor, massas, organomegalias), percussão...",      "order": 6},
        {"id": "resultados_exames",          "label": "Resultados de Exames",               "type": "textarea", "placeholder": "Endoscopia, colonoscopia, laboratorial (TGO, TGP, GGT, amilase, H. pylori)...", "order": 7},
        {"id": "hipotese_diagnostica",       "label": "Hipótese Diagnóstica",               "type": "textarea", "placeholder": "Descreva as hipóteses diagnósticas...",                                       "order": 8},
        {"id": "plano_tratamento",           "label": "Plano de Tratamento",                "type": "textarea", "placeholder": "Conduta terapêutica e orientações dietéticas...",                              "order": 9},
        {"id": "prescricoes",                "label": "Prescrições",                        "type": "textarea", "placeholder": "Liste os medicamentos prescritos com dose e posologia...",                   "order": 10},
        {"id": "orientacoes_dieteticas",     "label": "Orientações Dietéticas",             "type": "textarea", "placeholder": "Restrições e recomendações alimentares...",                                    "order": 11},
        {"id": "solicitacao_exames",         "label": "Solicitação de Exames",              "type": "textarea", "placeholder": "Endoscopia, colonoscopia, ultrassom abdominal, laboratorial...",               "order": 12},
        {"id": "solicitacao_encaminhamento", "label": "Solicitação de Encaminhamento",      "type": "textarea", "placeholder": "Descreva o encaminhamento solicitado...",                                     "order": 13},
        {"id": "atestado",                   "label": "Atestado",                           "type": "textarea", "placeholder": "Informe os dados do atestado...",                                             "order": 14}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- GINECOLOGISTA
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Ginecológico', 'GINECOLOGISTA',
    '[
        {"id": "queixa_principal",           "label": "Queixa Principal / Motivo da Consulta", "type": "textarea", "placeholder": "Descreva o motivo da consulta ou queixa principal...",                    "order": 1},
        {"id": "historia_menstrual",         "label": "História Menstrual",                 "type": "textarea", "placeholder": "DUM, ciclo (regular/irregular), duração, fluxo, dismenorreia, TPM...",       "order": 2},
        {"id": "historia_obstetrica",        "label": "História Obstétrica",                "type": "textarea", "placeholder": "Gestações (G), partos (P), abortos (A), tipo de parto, complicações...",    "order": 3},
        {"id": "metodo_contraceptivo",       "label": "Método Contraceptivo / ISTs",        "type": "textarea", "placeholder": "Método em uso, histórico de ISTs, parceiro fixo, uso de preservativo...",   "order": 4},
        {"id": "exames_preventivos",         "label": "Preventivo / Colposcopia",           "type": "textarea", "placeholder": "Data do último preventivo, resultado, colposcopia prévia...",                "order": 5},
        {"id": "exame_fisico",               "label": "Exame Físico",                       "type": "textarea", "placeholder": "Mamas, abdome, exame especular (colo, corrimento), toque vaginal e retal...", "order": 6},
        {"id": "hipotese_diagnostica",       "label": "Hipótese Diagnóstica",               "type": "textarea", "placeholder": "Descreva as hipóteses diagnósticas...",                                       "order": 7},
        {"id": "conduta_tratamento",         "label": "Conduta / Tratamento",               "type": "textarea", "placeholder": "Conduta proposta, orientações e acompanhamento...",                          "order": 8},
        {"id": "prescricoes",                "label": "Prescrições",                        "type": "textarea", "placeholder": "Liste os medicamentos prescritos com dose e posologia...",                   "order": 9},
        {"id": "solicitacao_exames",         "label": "Solicitação de Exames",              "type": "textarea", "placeholder": "Ultrassom pélvico/transvaginal, preventivo, colposcopia, laboratorial...",   "order": 10},
        {"id": "solicitacao_encaminhamento", "label": "Solicitação de Encaminhamento",      "type": "textarea", "placeholder": "Descreva o encaminhamento solicitado...",                                     "order": 11},
        {"id": "atestado",                   "label": "Atestado",                           "type": "textarea", "placeholder": "Informe os dados do atestado...",                                             "order": 12}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- MASTOLOGISTA
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Mastológico', 'MASTOLOGISTA',
    '[
        {"id": "queixa_principal",           "label": "Queixa Principal",                  "type": "textarea", "placeholder": "Nódulo, dor, descarga papilar, alteração de contorno ou pele...",              "order": 1},
        {"id": "historia_queixa",            "label": "História da Queixa Atual",           "type": "textarea", "placeholder": "Início, evolução, características, relação com ciclo menstrual...",            "order": 2},
        {"id": "fatores_risco",              "label": "Fatores de Risco para Câncer de Mama","type": "textarea", "placeholder": "Histórico familiar (1º e 2º grau), menarca, menopausa, TH, amamentação, biópsia prévia, BRCA...", "order": 3},
        {"id": "historico_exames_anteriores","label": "Histórico de Exames Anteriores",     "type": "textarea", "placeholder": "Última mamografia, ultrassom de mama, biópsia, BI-RADS anterior...",          "order": 4},
        {"id": "exame_fisico_mamas",         "label": "Exame Físico das Mamas",             "type": "textarea", "placeholder": "Inspeção (estática/dinâmica), palpação (nódulos, linfonodos axilares/supraclaviculares)...", "order": 5},
        {"id": "analise_imagem",             "label": "Análise de Imagem (BI-RADS)",        "type": "textarea", "placeholder": "Achados mamográficos e/ou ultrassonográficos, classificação BI-RADS...",       "order": 6},
        {"id": "hipotese_diagnostica",       "label": "Hipótese Diagnóstica",               "type": "textarea", "placeholder": "Descreva as hipóteses diagnósticas...",                                       "order": 7},
        {"id": "conduta_tratamento",         "label": "Conduta / Tratamento",               "type": "textarea", "placeholder": "Conduta proposta (acompanhamento, biópsia, cirurgia, hormonioterapia)...",    "order": 8},
        {"id": "prescricoes",                "label": "Prescrições",                        "type": "textarea", "placeholder": "Liste os medicamentos prescritos...",                                          "order": 9},
        {"id": "solicitacao_exames",         "label": "Solicitação de Exames",              "type": "textarea", "placeholder": "Mamografia, ultrassom de mama, ressonância, biópsia, marcadores tumorais...", "order": 10},
        {"id": "solicitacao_encaminhamento", "label": "Solicitação de Encaminhamento",      "type": "textarea", "placeholder": "Descreva o encaminhamento solicitado...",                                     "order": 11},
        {"id": "atestado",                   "label": "Atestado",                           "type": "textarea", "placeholder": "Informe os dados do atestado...",                                             "order": 12}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- OBSTETRIACO
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Obstétrico / Pré-Natal', 'OBSTETRIACO',
    '[
        {"id": "dados_gestacao",             "label": "Dados da Gestação",                  "type": "textarea", "placeholder": "DUM, DPP (Naegele/USG), IG atual, paridade (G/P/A), gemelar...",              "order": 1},
        {"id": "queixas_gestante",           "label": "Queixas da Gestante",                "type": "textarea", "placeholder": "Náusea, vômito, dor abdominal, sangramento, movimentação fetal, edema...",    "order": 2},
        {"id": "evolucao_gestacao",          "label": "Evolução da Gestação Atual",         "type": "textarea", "placeholder": "Intercorrências, hospitalizações, uso de medicamentos na gestação...",         "order": 3},
        {"id": "exame_fisico_obstetrico",    "label": "Exame Físico Obstétrico",            "type": "textarea", "placeholder": "PA, peso, edema, AU (altura uterina), BCF, apresentação fetal, dinâmica uterina...", "order": 4},
        {"id": "resultado_exames",           "label": "Resultados de Exames",               "type": "textarea", "placeholder": "Ultrassom (morfológico, doppler, perfil biofísico), laboratoriais (toxoplasmose, rubéola, VDRL, HIV, hemograma, glicemia)...", "order": 5},
        {"id": "conduta_orientacoes",        "label": "Conduta / Orientações",              "type": "textarea", "placeholder": "Orientações sobre alimentação, atividade física, sinais de alerta, retorno...", "order": 6},
        {"id": "prescricoes",                "label": "Prescrições",                        "type": "textarea", "placeholder": "Suplementos e medicamentos seguros na gestação (ácido fólico, ferro, etc.)...", "order": 7},
        {"id": "solicitacao_exames",         "label": "Solicitação de Exames",              "type": "textarea", "placeholder": "Ultrassom, laboratoriais do pré-natal, teste de tolerância à glicose...",     "order": 8},
        {"id": "observacoes",                "label": "Observações",                        "type": "textarea", "placeholder": "Observações adicionais...",                                                   "order": 9},
        {"id": "atestado",                   "label": "Atestado",                           "type": "textarea", "placeholder": "Informe os dados do atestado (gestacional, de repouso)...",                   "order": 10}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- OFTALMOLOGISTA
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Oftalmológico', 'OFTALMOLOGISTA',
    '[
        {"id": "queixa_principal",           "label": "Queixa Principal",                  "type": "textarea", "placeholder": "Baixa visão, olho vermelho, dor ocular, diplopia, fotofobia, floaters...",     "order": 1},
        {"id": "historia_doenca_ocular",     "label": "História da Doença Ocular Atual",   "type": "textarea", "placeholder": "Início, evolução, unilateral/bilateral, uso de colírios/medicamentos...",      "order": 2},
        {"id": "antecedentes_oftalmologicos","label": "Antecedentes Oftalmológicos",        "type": "textarea", "placeholder": "Cirurgias oculares, uso de óculos/lentes, doenças oculares prévias, trauma...", "order": 3},
        {"id": "acuidade_visual",            "label": "Acuidade Visual",                   "type": "textarea", "placeholder": "OD: sc / cc — OE: sc / cc (s/c = sem correção, c/c = com correção)...",       "order": 4},
        {"id": "biomicroscopia",             "label": "Biomicroscopia (Segmento Anterior)", "type": "textarea", "placeholder": "Córnea, câmara anterior, íris, cristalino, vítreo anterior — OD e OE...",     "order": 5},
        {"id": "tonometria",                 "label": "Tonometria (PIO)",                   "type": "textarea", "placeholder": "PIO OD: ___ mmHg — PIO OE: ___ mmHg (horário da medida)...",                  "order": 6},
        {"id": "fundoscopia",                "label": "Fundoscopia / Mapeamento de Retina", "type": "textarea", "placeholder": "Papila, escavação, vasos, mácula, retina periférica — OD e OE...",             "order": 7},
        {"id": "refracao",                   "label": "Refração / Prescrição de Óculos",   "type": "textarea", "placeholder": "OD: esf / cil / eixo — OE: esf / cil / eixo — Add (se necessário)...",        "order": 8},
        {"id": "hipotese_diagnostica",       "label": "Hipótese Diagnóstica",               "type": "textarea", "placeholder": "Descreva as hipóteses diagnósticas...",                                       "order": 9},
        {"id": "conduta_tratamento",         "label": "Conduta / Tratamento",               "type": "textarea", "placeholder": "Colírios, medicamentos, procedimento ou cirurgia indicada...",                "order": 10},
        {"id": "prescricoes",                "label": "Prescrições",                        "type": "textarea", "placeholder": "Colírios e medicamentos prescritos com posologia...",                          "order": 11},
        {"id": "solicitacao_exames",         "label": "Solicitação de Exames",              "type": "textarea", "placeholder": "OCT, campimetria computadorizada, topografia, angiofluoresceinografia...",    "order": 12},
        {"id": "atestado",                   "label": "Atestado",                           "type": "textarea", "placeholder": "Informe os dados do atestado...",                                             "order": 13}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- UROLOGISTA
INSERT INTO medical_record_templates (id, tenant_id, name, professional_type, schema, is_readonly, active, created_at, updated_at)
VALUES (
    gen_random_uuid(), NULL, 'Prontuário Urológico', 'UROLOGISTA',
    '[
        {"id": "queixa_principal",           "label": "Queixa Principal",                  "type": "textarea", "placeholder": "Descreva a queixa principal (dor, hematúria, disfunção erétil, infecção...)...", "order": 1},
        {"id": "historia_doenca_atual",      "label": "História da Doença Atual",           "type": "textarea", "placeholder": "Evolução dos sintomas, início, fatores de melhora/piora...",                   "order": 2},
        {"id": "sintomas_urinarios",         "label": "Sintomas do Trato Urinário (STUI)",  "type": "textarea", "placeholder": "Frequência, urgência, noctúria, disúria, hesitância, jato fraco, esforço miccional, gotejamento terminal...", "order": 3},
        {"id": "antecedentes_urologicos",    "label": "Antecedentes Urológicos",            "type": "textarea", "placeholder": "Litíase, ITUs de repetição, cirurgias urológicas, DSTs, trauma...",            "order": 4},
        {"id": "medicamentos_em_uso",        "label": "Medicamentos em Uso",                "type": "textarea", "placeholder": "Especialmente alfa-bloqueadores, inibidores de 5-alfa-redutase, anticolinérgicos...", "order": 5},
        {"id": "exame_fisico",               "label": "Exame Físico",                       "type": "textarea", "placeholder": "Abdome, flancos, genitália externa, toque retal (volume e consistência prostática)...", "order": 6},
        {"id": "resultados_exames",          "label": "Resultados de Exames",               "type": "textarea", "placeholder": "PSA total/livre, urofluxometria, USG renal/vesical/próstata, urocultura, urina tipo I...", "order": 7},
        {"id": "hipotese_diagnostica",       "label": "Hipótese Diagnóstica",               "type": "textarea", "placeholder": "Descreva as hipóteses diagnósticas...",                                       "order": 8},
        {"id": "plano_tratamento",           "label": "Plano de Tratamento",                "type": "textarea", "placeholder": "Conduta farmacológica, intervencionista ou expectante...",                     "order": 9},
        {"id": "prescricoes",                "label": "Prescrições",                        "type": "textarea", "placeholder": "Liste os medicamentos prescritos com dose e posologia...",                   "order": 10},
        {"id": "solicitacao_exames",         "label": "Solicitação de Exames",              "type": "textarea", "placeholder": "PSA, urocultura, USG, urotomografia, uretrocistoscopia, biópsia de próstata...", "order": 11},
        {"id": "solicitacao_encaminhamento", "label": "Solicitação de Encaminhamento",      "type": "textarea", "placeholder": "Descreva o encaminhamento solicitado...",                                     "order": 12},
        {"id": "atestado",                   "label": "Atestado",                           "type": "textarea", "placeholder": "Informe os dados do atestado...",                                             "order": 13}
    ]'::jsonb,
    TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

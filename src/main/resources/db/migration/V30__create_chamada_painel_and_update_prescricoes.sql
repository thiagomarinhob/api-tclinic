-- =====================================================
-- V30: Cria tabela chamada_painel e atualiza tipos de
--      campos de prescrição nos templates existentes.
-- =====================================================

-- ----- 1. Tabela chamada_painel -----

CREATE TABLE chamada_painel (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id  UUID        NOT NULL REFERENCES appointments(id),
    room_id         UUID        NOT NULL REFERENCES rooms(id),
    numero_chamada  SMALLINT    NOT NULL,
    hora_chamada    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    hora_atendido   TIMESTAMPTZ
);

CREATE INDEX idx_chamada_painel_data ON chamada_painel(hora_chamada DESC);

-- ----- 2. Atualiza tipos dos campos de prescrição nos templates -----

-- Receita Comum: todos os templates com campo "prescricoes", exceto Psiquiátrico
UPDATE medical_record_templates
SET schema = (
    SELECT jsonb_agg(
        CASE
            WHEN elem->>'id' = 'prescricoes'
            THEN elem || '{"type": "prescricao_comum"}'::jsonb
            ELSE elem
        END
    )
    FROM jsonb_array_elements(schema) AS elem
),
    updated_at = CURRENT_TIMESTAMP
WHERE schema @> '[{"id": "prescricoes"}]'::jsonb
  AND name != 'Prontuário Psiquiátrico';

-- Receita Controlada: apenas o template psiquiátrico
UPDATE medical_record_templates
SET schema = (
    SELECT jsonb_agg(
        CASE
            WHEN elem->>'id' = 'prescricoes'
            THEN elem || '{"type": "prescricao_controlada"}'::jsonb
            ELSE elem
        END
    )
    FROM jsonb_array_elements(schema) AS elem
),
    updated_at = CURRENT_TIMESTAMP
WHERE name = 'Prontuário Psiquiátrico';

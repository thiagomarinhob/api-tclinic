-- =====================================================
-- Remove o campo "atestado" do schema de todos os templates de prontuário.
-- O atestado já possui fluxo próprio via botão de ação rápida (AtestadoDialog),
-- tornando o campo de texto redundante.
-- =====================================================

UPDATE medical_record_templates
SET schema = (
    SELECT jsonb_agg(elem ORDER BY (elem->>'order')::int)
    FROM jsonb_array_elements(schema) AS elem
    WHERE elem->>'id' != 'atestado'
),
updated_at = CURRENT_TIMESTAMP
WHERE schema @> '[{"id": "atestado"}]'::jsonb;

-- =====================================================
-- V33: Cria registro de Professional para qualquer
--      usuário solo que ainda não possua um.
--      Cobre contas criadas antes da V32/correção do
--      DefaultSignUpSoloUseCase.
-- =====================================================

INSERT INTO professionals (id, tenant_id, user_id, specialty, active, created_at, updated_at)
SELECT
    gen_random_uuid(),
    t.id,
    u.id,
    'CLINICO_GERAL',
    true,
    NOW(),
    NOW()
FROM tenant t
JOIN user_tenant_role utr ON utr.tenant_id = t.id
JOIN users            u   ON u.id = utr.user_id
WHERE t.type = 'SOLO'
  AND NOT EXISTS (
      SELECT 1
      FROM professionals p
      WHERE p.tenant_id = t.id
        AND p.user_id   = u.id
  );

-- =====================================================
-- V32: Torna document_type e document_number opcionais
--      na tabela professionals para suportar profissionais
--      criados via signup solo sem dados de registro ainda.
-- =====================================================

ALTER TABLE professionals
    ALTER COLUMN document_type   DROP NOT NULL,
    ALTER COLUMN document_number DROP NOT NULL;

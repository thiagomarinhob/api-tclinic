-- =====================================================
-- V9: Adiciona campo health_plan à tabela patients
-- =====================================================
ALTER TABLE patients ADD COLUMN health_plan VARCHAR(255);

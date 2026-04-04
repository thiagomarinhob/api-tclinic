-- Script para corrigir a coluna birth_date na tabela users
-- Este script permite que a coluna birth_date aceite valores NULL
-- Execute este script no banco de dados PostgreSQL

ALTER TABLE users ALTER COLUMN birth_date DROP NOT NULL;

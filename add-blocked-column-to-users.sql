-- Script para adicionar a coluna blocked na tabela users
-- Este script adiciona a coluna blocked para controle de acesso dos usuários
-- Execute este script no banco de dados PostgreSQL

ALTER TABLE users ADD COLUMN IF NOT EXISTS blocked BOOLEAN NOT NULL DEFAULT false;

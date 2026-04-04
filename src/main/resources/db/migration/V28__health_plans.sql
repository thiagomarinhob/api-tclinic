-- =====================================================
-- Convênios: atualização do payment_type e nova tabela
-- =====================================================

-- 1. Atualiza constraint de payment_type para incluir novos tipos
ALTER TABLE lab_orders DROP CONSTRAINT IF EXISTS chk_lab_order_payment;
ALTER TABLE lab_orders ADD CONSTRAINT chk_lab_order_payment
    CHECK (payment_type IN ('PIX', 'CREDIT_CARD', 'DEBIT_CARD', 'BOLETO', 'HEALTH_PLAN', 'PRIVATE'));

-- 2. Tabela de convênios da clínica
CREATE TABLE health_plans (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      UUID NOT NULL,
    name           VARCHAR(255) NOT NULL,
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_health_plans_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
);

-- 3. Vincula pedido de laboratório ao convênio selecionado (opcional)
ALTER TABLE lab_orders ADD COLUMN health_plan_id UUID;
ALTER TABLE lab_orders ADD CONSTRAINT fk_lab_orders_health_plan
    FOREIGN KEY (health_plan_id) REFERENCES health_plans(id);

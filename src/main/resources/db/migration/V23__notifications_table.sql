-- Tabela: notifications
-- Notificações in-app por tenant (confirmação/cancelamento de consultas)
-- =====================================================
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    reference_type VARCHAR(32),
    reference_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT chk_notification_type CHECK (type IN ('APPOINTMENT_CONFIRMATION', 'APPOINTMENT_CANCELLATION'))
);

CREATE INDEX idx_notifications_tenant_id ON notifications(tenant_id);
CREATE INDEX idx_notifications_tenant_read ON notifications(tenant_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

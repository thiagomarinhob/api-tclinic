-- =====================================================
-- Laboratório: Tipos de exame (catálogo)
-- =====================================================
CREATE TABLE lab_exam_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    code VARCHAR(50),
    name VARCHAR(255) NOT NULL,
    sector VARCHAR(50) NOT NULL DEFAULT 'OTHER',
    sample_type VARCHAR(50),
    unit VARCHAR(50),
    reference_range_text TEXT,
    preparation_info TEXT,
    turnaround_hours INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lab_exam_types_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
);

CREATE INDEX idx_lab_exam_types_tenant ON lab_exam_types(tenant_id);
CREATE INDEX idx_lab_exam_types_active ON lab_exam_types(tenant_id, active);

-- =====================================================
-- Laboratório: Pedidos
-- =====================================================
CREATE TABLE lab_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    appointment_id UUID,
    professional_id UUID,
    requester_name VARCHAR(255),
    priority VARCHAR(20) NOT NULL DEFAULT 'ROUTINE',
    payment_type VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    health_plan_name VARCHAR(255),
    clinical_notes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    sample_code VARCHAR(100),
    collected_at TIMESTAMP,
    collected_by VARCHAR(255),
    received_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lab_orders_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_lab_orders_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_lab_orders_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL,
    CONSTRAINT fk_lab_orders_professional FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE SET NULL,
    CONSTRAINT chk_lab_order_status CHECK (status IN ('REQUESTED','COLLECTED','RECEIVED','IN_ANALYSIS','COMPLETED','CANCELLED')),
    CONSTRAINT chk_lab_order_priority CHECK (priority IN ('ROUTINE','URGENT')),
    CONSTRAINT chk_lab_order_payment CHECK (payment_type IN ('HEALTH_PLAN','PRIVATE'))
);

CREATE INDEX idx_lab_orders_tenant ON lab_orders(tenant_id);
CREATE INDEX idx_lab_orders_patient ON lab_orders(patient_id);
CREATE INDEX idx_lab_orders_status ON lab_orders(tenant_id, status);

-- =====================================================
-- Laboratório: Itens do pedido (exame + resultado)
-- =====================================================
CREATE TABLE lab_order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    exam_type_id UUID,
    exam_name VARCHAR(255) NOT NULL,
    sector VARCHAR(50) NOT NULL DEFAULT 'OTHER',
    sample_type VARCHAR(50),
    unit VARCHAR(50),
    reference_range_text TEXT,
    result_value TEXT,
    result_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    is_abnormal BOOLEAN,
    is_critical BOOLEAN DEFAULT FALSE,
    technical_validated_at TIMESTAMP,
    technical_validated_by VARCHAR(255),
    final_validated_at TIMESTAMP,
    final_validated_by VARCHAR(255),
    observations TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lab_order_items_order FOREIGN KEY (order_id) REFERENCES lab_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_lab_order_items_exam_type FOREIGN KEY (exam_type_id) REFERENCES lab_exam_types(id) ON DELETE SET NULL,
    CONSTRAINT chk_lab_item_result_status CHECK (result_status IN ('PENDING','ENTERED','TECHNICAL_VALIDATED','RELEASED'))
);

CREATE INDEX idx_lab_order_items_order ON lab_order_items(order_id);
CREATE INDEX idx_lab_order_items_status ON lab_order_items(result_status);

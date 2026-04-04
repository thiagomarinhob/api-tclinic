-- =====================================================
-- Tabela: medical_documents
-- Documentos médicos gerados via Memed (prescrições, atestados, etc.)
-- =====================================================
CREATE TABLE medical_documents (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL,
    document_url VARCHAR(500) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    source VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_medical_documents_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

CREATE INDEX idx_medical_documents_appointment ON medical_documents(appointment_id);

ALTER TABLE appointments ADD COLUMN whatsapp_message_id VARCHAR(255) NULL;
CREATE INDEX idx_appointments_whatsapp_message_id ON appointments(whatsapp_message_id) WHERE whatsapp_message_id IS NOT NULL;

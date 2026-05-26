-- V34: Add combo support to procedures

ALTER TABLE procedures ADD COLUMN is_combo BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE procedure_combo_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    combo_procedure_id UUID NOT NULL,
    item_procedure_id  UUID NOT NULL,
    CONSTRAINT fk_combo_procedure FOREIGN KEY (combo_procedure_id) REFERENCES procedures(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_procedure  FOREIGN KEY (item_procedure_id)  REFERENCES procedures(id) ON DELETE RESTRICT
);

CREATE INDEX idx_combo_items_combo_id ON procedure_combo_items(combo_procedure_id);

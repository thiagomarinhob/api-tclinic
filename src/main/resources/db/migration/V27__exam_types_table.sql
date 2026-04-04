-- =====================================================
-- Catálogo global de tipos de exame
-- =====================================================
CREATE TABLE exam_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO exam_types (category, name, display_order) VALUES
('Exames de Imagem', 'Densidade Óssea', 1),
('Exames de Imagem', 'Ecocardiograma', 2),
('Exames de Imagem', 'Eletrocardiograma', 3),
('Exames de Imagem', 'Mamografia', 4),
('Exames de Imagem', 'Papanicolau', 5),
('Exames de Imagem', 'Raio-X', 6),
('Exames de Imagem', 'Teste Ergométrico', 7),
('Exames de Imagem', 'Tomografia', 8),
('Exames de Imagem', 'Ultrassonografia - Abdome', 9),
('Exames de Imagem', 'Ultrassonografia - Pélvica', 10),
('Exames de Imagem', 'Ultrassonografia - Tireoide', 11),
('Exames Laboratoriais', 'Ácido Úrico', 1),
('Exames Laboratoriais', 'Exames de Fezes', 2),
('Exames Laboratoriais', 'Exames de Urina', 3),
('Exames Laboratoriais', 'Função Hepática', 4),
('Exames Laboratoriais', 'Função Renal', 5),
('Exames Laboratoriais', 'Função Tireoidiana', 6),
('Exames Laboratoriais', 'Glicemia de Jejum', 7),
('Exames Laboratoriais', 'Hemoglobina Glicada', 8),
('Exames Laboratoriais', 'Hemograma Completo', 9),
('Exames Laboratoriais', 'Perfil Lipídico', 10),
('Exames Laboratoriais', 'Sorologias / Infecções', 11),
('Laudo Psiquiátrico', 'Laudo Psiquiátrico', 1),
('Exames Admissionais', 'ALT', 1),
('Exames Admissionais', 'AST', 2),
('Exames Admissionais', 'Creatinina', 3),
('Exames Admissionais', 'EAS', 4),
('Exames Admissionais', 'Gama-GT', 5),
('Exames Admissionais', 'Perfil Lipídico', 6);

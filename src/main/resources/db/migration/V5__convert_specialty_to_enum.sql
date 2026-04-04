-- Converter valores existentes de texto livre para os valores do enum
UPDATE professionals SET specialty = 'CARDIOLOGISTA' WHERE LOWER(specialty) IN ('cardiologista', 'cardiologia');
UPDATE professionals SET specialty = 'CLINICO_GERAL' WHERE LOWER(specialty) IN ('clinico geral', 'clínico geral', 'clinico_geral');
UPDATE professionals SET specialty = 'DENTISTA' WHERE LOWER(specialty) IN ('dentista', 'odontologia', 'odontologista');
UPDATE professionals SET specialty = 'DERMATOLOGISTA' WHERE LOWER(specialty) IN ('dermatologista', 'dermatologia');
UPDATE professionals SET specialty = 'ENDOCRINOLOGISTA' WHERE LOWER(specialty) IN ('endocrinologista', 'endocrinologia');
UPDATE professionals SET specialty = 'ENFERMEIRO' WHERE LOWER(specialty) IN ('enfermeiro', 'enfermeira', 'enfermagem');
UPDATE professionals SET specialty = 'FISIOTERAPEUTA' WHERE LOWER(specialty) IN ('fisioterapeuta', 'fisioterapia');
UPDATE professionals SET specialty = 'GASTROENTEROLOGISTA' WHERE LOWER(specialty) IN ('gastroenterologista', 'gastroenterologia');
UPDATE professionals SET specialty = 'GINECOLOGISTA' WHERE LOWER(specialty) IN ('ginecologista', 'ginecologia');
UPDATE professionals SET specialty = 'MASTOLOGISTA' WHERE LOWER(specialty) IN ('mastologista', 'mastologia');
UPDATE professionals SET specialty = 'OBSTETRIACO' WHERE LOWER(specialty) IN ('obstetriaco', 'obstétrico', 'obstetra', 'obstetrícia', 'obstetricia');
UPDATE professionals SET specialty = 'OFTALMOLOGISTA' WHERE LOWER(specialty) IN ('oftalmologista', 'oftalmologia');
UPDATE professionals SET specialty = 'PEDIATRA' WHERE LOWER(specialty) IN ('pediatra', 'pediatria');
UPDATE professionals SET specialty = 'PSICOLOGO' WHERE LOWER(specialty) IN ('psicologo', 'psicólogo', 'psicologia');
UPDATE professionals SET specialty = 'PSICOLOGISTA' WHERE LOWER(specialty) IN ('psicologista');
UPDATE professionals SET specialty = 'UROLOGISTA' WHERE LOWER(specialty) IN ('urologista', 'urologia');

-- Alterar a coluna para varchar(30) para acomodar os nomes do enum
ALTER TABLE professionals ALTER COLUMN specialty TYPE VARCHAR(30);

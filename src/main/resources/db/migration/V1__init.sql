-- =====================================================
-- Solutions Clinic - Migration Inicial
-- Criação do schema completo do banco de dados
-- =====================================================

-- =====================================================
-- Tabela: users
-- Armazena os usuários do sistema
-- =====================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone VARCHAR(255),
    cpf VARCHAR(11) UNIQUE,
    birth_date VARCHAR(10),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    blocked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_cpf ON users(cpf);

-- =====================================================
-- Tabela: tenant
-- Armazena os tenants (clínicas/profissionais solo)
-- =====================================================
CREATE TABLE tenant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    cnpj VARCHAR(255) UNIQUE,
    plan_type VARCHAR(20),
    address VARCHAR(255),
    phone VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT FALSE,
    subdomain VARCHAR(64) UNIQUE,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_SETUP',
    trial_ends_at DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_tenant_plan_type CHECK (plan_type IN ('BASIC', 'PRO', 'CUSTOM')),
    CONSTRAINT chk_tenant_type CHECK (type IN ('CLINIC', 'SOLO')),
    CONSTRAINT chk_tenant_status CHECK (status IN ('PENDING_SETUP', 'TRIAL', 'ACTIVE', 'SUSPENDED', 'CANCELED'))
);

CREATE INDEX idx_tenant_subdomain ON tenant(subdomain);
CREATE INDEX idx_tenant_status ON tenant(status);

-- =====================================================
-- Tabela: user_tenant_role
-- Relacionamento entre usuários e tenants com roles
-- =====================================================
CREATE TABLE user_tenant_role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(16) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_utr_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_utr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_tenant_role UNIQUE (user_id, tenant_id, role),
    CONSTRAINT chk_utr_role CHECK (role IN ('OWNER', 'ADMIN', 'RECEPTION', 'SPECIALIST', 'FINANCE', 'READONLY'))
);

CREATE INDEX idx_utr_tenant_id ON user_tenant_role(tenant_id);
CREATE INDEX idx_utr_user_id ON user_tenant_role(user_id);

-- =====================================================
-- Tabela: subscriptions
-- Armazena as assinaturas dos tenants (Stripe)
-- =====================================================
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    plan_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    stripe_checkout_session_id VARCHAR(255) UNIQUE,
    stripe_subscription_id VARCHAR(255) UNIQUE,
    stripe_customer_id VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,
    canceled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_subscriptions_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT chk_subscription_plan_type CHECK (plan_type IN ('BASIC', 'PRO', 'CUSTOM')),
    CONSTRAINT chk_subscription_status CHECK (status IN ('PENDING', 'ACTIVE', 'CANCELED', 'PAST_DUE', 'UNPAID'))
);

CREATE INDEX idx_subscriptions_tenant_id ON subscriptions(tenant_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);

-- =====================================================
-- Tabela: rooms
-- Armazena as salas/consultórios do tenant
-- =====================================================
CREATE TABLE rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    capacity INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_rooms_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
);

CREATE INDEX idx_rooms_tenant_id ON rooms(tenant_id);

-- =====================================================
-- Tabela: patients
-- Armazena os pacientes do tenant
-- =====================================================
CREATE TABLE patients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    cpf VARCHAR(11),
    birth_date VARCHAR(10),
    gender VARCHAR(20),
    email VARCHAR(255),
    phone VARCHAR(255),
    whatsapp VARCHAR(255),
    address_street VARCHAR(255),
    address_number VARCHAR(255),
    address_complement VARCHAR(255),
    address_neighborhood VARCHAR(255),
    address_city VARCHAR(255),
    address_state VARCHAR(255),
    address_zipcode VARCHAR(255),
    blood_type VARCHAR(10),
    allergies TEXT,
    guardian_name VARCHAR(255),
    guardian_phone VARCHAR(255),
    guardian_relationship VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_patients_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT uk_patient_cpf_tenant UNIQUE (cpf, tenant_id),
    CONSTRAINT chk_patient_gender CHECK (gender IN ('MASCULINO', 'FEMININO', 'OUTRO', 'NAO_INFORMADO')),
    CONSTRAINT chk_patient_blood_type CHECK (blood_type IN ('A_POSITIVE', 'A_NEGATIVE', 'B_POSITIVE', 'B_NEGATIVE', 'AB_POSITIVE', 'AB_NEGATIVE', 'O_POSITIVE', 'O_NEGATIVE'))
);

CREATE INDEX idx_patients_tenant_id ON patients(tenant_id);
CREATE INDEX idx_patients_cpf ON patients(cpf);
CREATE INDEX idx_patients_first_name ON patients(first_name);

-- =====================================================
-- Tabela: professionals
-- Armazena os profissionais do tenant
-- =====================================================
CREATE TABLE professionals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    specialty VARCHAR(255) NOT NULL,
    document_type VARCHAR(10) NOT NULL,
    document_number VARCHAR(255) NOT NULL,
    document_state VARCHAR(255),
    bio TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_professionals_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_professionals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_professional_user_tenant UNIQUE (user_id, tenant_id),
    CONSTRAINT chk_professional_document_type CHECK (document_type IN ('CRM', 'CREFITO', 'CRO', 'CRP', 'CRN', 'COREN', 'OUTRO'))
);

CREATE INDEX idx_professionals_tenant_id ON professionals(tenant_id);
CREATE INDEX idx_professionals_user_id ON professionals(user_id);

-- =====================================================
-- Tabela: professional_schedules
-- Armazena os horários de atendimento dos profissionais
-- =====================================================
CREATE TABLE professional_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    professional_id UUID NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    lunch_break_start TIME NOT NULL,
    lunch_break_end TIME NOT NULL,
    slot_duration_minutes INTEGER NOT NULL DEFAULT 30,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_professional_schedules_professional FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE CASCADE,
    CONSTRAINT uk_professional_schedule_day UNIQUE (professional_id, day_of_week),
    CONSTRAINT chk_schedule_day_of_week CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'))
);

CREATE INDEX idx_professional_schedules_professional_id ON professional_schedules(professional_id);

-- =====================================================
-- Tabela: procedures
-- Armazena os procedimentos oferecidos pelos profissionais
-- =====================================================
CREATE TABLE procedures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    professional_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    estimated_duration_minutes INTEGER NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    professional_commission_percent DECIMAL(5, 2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_procedures_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_procedures_professional FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE CASCADE
);

CREATE INDEX idx_procedures_tenant_id ON procedures(tenant_id);
CREATE INDEX idx_procedures_professional_id ON procedures(professional_id);

-- =====================================================
-- Tabela: appointments
-- Armazena os agendamentos
-- =====================================================
CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    professional_id UUID NOT NULL,
    room_id UUID,
    scheduled_at TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    observations TEXT,
    cancelled_at TIMESTAMP,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    duration_actual_minutes INTEGER,
    total_value DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(20),
    payment_status VARCHAR(20) NOT NULL,
    paid_at TIMESTAMP,
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_appointments_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointments_professional FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointments_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL,
    CONSTRAINT fk_appointments_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT chk_appointment_status CHECK (status IN ('AGENDADO', 'CONFIRMADO', 'EM_ATENDIMENTO', 'FINALIZADO', 'CANCELADO', 'NAO_COMPARECEU')),
    CONSTRAINT chk_appointment_payment_method CHECK (payment_method IN ('PIX', 'DEBITO', 'CREDITO', 'DINHEIRO', 'BOLETO', 'OUTRO')),
    CONSTRAINT chk_appointment_payment_status CHECK (payment_status IN ('PENDENTE', 'PAGO', 'CANCELADO', 'FIADO'))
);

CREATE INDEX idx_appointments_tenant_id ON appointments(tenant_id);
CREATE INDEX idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX idx_appointments_professional_id ON appointments(professional_id);
CREATE INDEX idx_appointments_scheduled_at ON appointments(scheduled_at);
CREATE INDEX idx_appointments_status ON appointments(status);

-- =====================================================
-- Tabela: appointment_procedures
-- Relacionamento entre agendamentos e procedimentos
-- =====================================================
CREATE TABLE appointment_procedures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID NOT NULL,
    procedure_id UUID NOT NULL,
    final_price DECIMAL(10, 2) NOT NULL,
    observations TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_appointment_procedures_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_procedures_procedure FOREIGN KEY (procedure_id) REFERENCES procedures(id) ON DELETE CASCADE
);

CREATE INDEX idx_appointment_procedures_appointment_id ON appointment_procedures(appointment_id);
CREATE INDEX idx_appointment_procedures_procedure_id ON appointment_procedures(procedure_id);

-- =====================================================
-- Tabela: financial_categories
-- Categorias de transações financeiras
-- =====================================================
CREATE TABLE financial_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_financial_categories_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT uk_financial_category_name_tenant UNIQUE (name, tenant_id),
    CONSTRAINT chk_financial_category_type CHECK (type IN ('INCOME', 'EXPENSE'))
);

CREATE INDEX idx_financial_categories_tenant_id ON financial_categories(tenant_id);

-- =====================================================
-- Tabela: financial_transactions
-- Transações financeiras (receitas e despesas)
-- =====================================================
CREATE TABLE financial_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    description VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    category_id UUID,
    amount DECIMAL(10, 2) NOT NULL,
    due_date DATE NOT NULL,
    payment_date DATE,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20),
    appointment_id UUID,
    professional_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_financial_transactions_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_financial_transactions_category FOREIGN KEY (category_id) REFERENCES financial_categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_financial_transactions_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL,
    CONSTRAINT fk_financial_transactions_professional FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE SET NULL,
    CONSTRAINT chk_financial_transaction_type CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT chk_financial_transaction_status CHECK (status IN ('PENDENTE', 'PAGO', 'CANCELADO', 'FIADO')),
    CONSTRAINT chk_financial_transaction_payment_method CHECK (payment_method IN ('PIX', 'DEBITO', 'CREDITO', 'DINHEIRO', 'BOLETO', 'OUTRO'))
);

CREATE INDEX idx_financial_transactions_tenant_id ON financial_transactions(tenant_id);
CREATE INDEX idx_financial_transactions_due_date ON financial_transactions(due_date);
CREATE INDEX idx_financial_transactions_status ON financial_transactions(status);
CREATE INDEX idx_financial_transactions_type ON financial_transactions(type);

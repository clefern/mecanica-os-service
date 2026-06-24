-- Enable uuid generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    account_status BOOLEAN NOT NULL DEFAULT TRUE,
    user_type VARCHAR(30) NOT NULL,
    role VARCHAR(30),
    last_login TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

CREATE TABLE clientes (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    documento VARCHAR(20) NOT NULL UNIQUE,
    tipo_pessoa VARCHAR(20) NOT NULL DEFAULT 'FISICA',
    telefone VARCHAR(30),
    endereco TEXT
);

CREATE INDEX idx_clientes_documento ON clientes(documento);

CREATE TABLE mecanicos (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    especialidade VARCHAR(60) NOT NULL
);

CREATE INDEX idx_mecanicos_cpf ON mecanicos(cpf);

CREATE TABLE atendentes (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    cpf VARCHAR(14) NOT NULL UNIQUE
);

CREATE INDEX idx_atendentes_cpf ON atendentes(cpf);

CREATE TABLE admins (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE veiculos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    placa VARCHAR(10) NOT NULL UNIQUE,
    marca VARCHAR(60) NOT NULL,
    modelo VARCHAR(60) NOT NULL,
    ano INTEGER CHECK (ano >= 1900 AND ano <= EXTRACT(YEAR FROM NOW()) + 1),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_veiculos_cliente ON veiculos(cliente_id);

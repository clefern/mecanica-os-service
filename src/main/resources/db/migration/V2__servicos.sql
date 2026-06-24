CREATE TABLE servicos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    valor_base NUMERIC(19, 2) NOT NULL,
    tempo_estimado_minutos BIGINT NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_servico_nome ON servicos(nome);

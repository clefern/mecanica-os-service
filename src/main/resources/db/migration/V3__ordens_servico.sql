CREATE TABLE ordens_servico (
    id UUID PRIMARY KEY,
    cliente_id UUID NOT NULL,
    veiculo_id UUID NOT NULL,
    mecanico_execucao_id UUID,
    mecanico_diagnostico_id UUID,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    valor_total DECIMAL(19, 2) NOT NULL DEFAULT 0,
    data_entrada TIMESTAMP NOT NULL,
    data_previsao TIMESTAMP,
    data_fechamento TIMESTAMP,
    data_aprovacao TIMESTAMP,
    observacoes TEXT,
    prioridade INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_os_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_os_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos(id),
    CONSTRAINT fk_os_mecanico_exec FOREIGN KEY (mecanico_execucao_id) REFERENCES mecanicos(id),
    CONSTRAINT fk_os_mecanico_diag FOREIGN KEY (mecanico_diagnostico_id) REFERENCES mecanicos(id)
);

CREATE INDEX idx_os_cliente ON ordens_servico(cliente_id);
CREATE INDEX idx_os_veiculo ON ordens_servico(veiculo_id);
CREATE INDEX idx_os_status ON ordens_servico(status);
CREATE INDEX idx_os_status_data_entrada ON ordens_servico(status, data_entrada ASC);

CREATE TABLE itens_ordem_servico (
    id UUID PRIMARY KEY,
    ordem_servico_id UUID NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor_unitario DECIMAL(19, 2) NOT NULL,
    quantidade INTEGER NOT NULL,
    referencia_id UUID NOT NULL,
    CONSTRAINT fk_itens_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id)
);

CREATE INDEX idx_itens_os_id ON itens_ordem_servico(ordem_servico_id);

CREATE TABLE os_history (
    id UUID PRIMARY KEY,
    ordem_servico_id UUID NOT NULL REFERENCES ordens_servico(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMPTZ
);

CREATE INDEX idx_os_history_os_id ON os_history(ordem_servico_id);

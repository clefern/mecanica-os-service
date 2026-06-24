CREATE TABLE saga_state (
    id UUID PRIMARY KEY,
    os_id UUID NOT NULL REFERENCES ordens_servico(id),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_saga_os_id ON saga_state(os_id);
CREATE INDEX idx_saga_status ON saga_state(status);

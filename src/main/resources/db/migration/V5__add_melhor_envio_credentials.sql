CREATE TABLE IF NOT EXISTS melhor_envio_credentials (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    access_token TEXT,
    refresh_token TEXT,
    token_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_melhor_envio_credentials_id ON melhor_envio_credentials(id);

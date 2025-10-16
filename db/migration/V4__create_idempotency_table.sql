CREATE TABLE IF NOT EXISTS idempotencies
(
    id UUID PRIMARY KEY,
    response CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

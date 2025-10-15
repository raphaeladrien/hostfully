CREATE TABLE IF NOT EXISTS properties
(
    id BIGINT PRIMARY KEY,
    external_id VARCHAR(12) NOT NULL,
    description VARCHAR(250) NOT NULL,
    alias VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS property_external_id_idx ON properties (external_id);

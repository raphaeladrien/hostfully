CREATE TABLE IF NOT EXISTS blocks
(
    id BIGINT PRIMARY KEY,
    external_id VARCHAR(12) NOT NULL UNIQUE,
    reason VARCHAR(50) NOT NULL,
    property_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT blocks_property_id_fk FOREIGN KEY (property_id) REFERENCES properties(id),
    CONSTRAINT blocks_chk_dates CHECK (end_date >= start_date),
    CONSTRAINT blocks_property_start_end_uq UNIQUE(property_id, start_date, end_date)
);

CREATE UNIQUE INDEX IF NOT EXISTS blocks_external_id_idx ON blocks (external_id);
CREATE UNIQUE INDEX IF NOT EXISTS blocks_property_id_idx ON blocks (property_id);

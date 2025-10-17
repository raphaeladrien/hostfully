CREATE TABLE IF NOT EXISTS bookings
(
    id BIGINT PRIMARY KEY,
    external_id VARCHAR(12) NOT NULL UNIQUE,
    property_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    guest_name VARCHAR(55) NOT NULL,
    number_guest INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT bookings_chk_dates CHECK (end_date > start_date),
    CONSTRAINT bookings_property_id_fk FOREIGN KEY (property_id) REFERENCES properties(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS bookings_external_id_idx ON bookings (external_id);
CREATE UNIQUE INDEX IF NOT EXISTS bookings_property_id_idx ON bookings (property_id);

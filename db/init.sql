CREATE TABLE logs (
    id SERIAL PRIMARY KEY,
    log_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    runtime_id VARCHAR(255) NOT NULL,
    log_level VARCHAR(10) NOT NULL,
    message TEXT NOT NULL,
    exception TEXT
)
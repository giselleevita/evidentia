CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE incident (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(255) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(255),
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(255),
    review_notes TEXT
);

CREATE INDEX idx_incident_tenant_id ON incident(tenant_id);
CREATE INDEX idx_incident_status ON incident(status);
CREATE INDEX idx_incident_severity ON incident(severity);
CREATE INDEX idx_incident_created_at ON incident(created_at);

CREATE TABLE incident_update (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id UUID NOT NULL REFERENCES incident(id) ON DELETE CASCADE,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    notes TEXT
);

CREATE INDEX idx_incident_update_incident_id ON incident_update(incident_id);
CREATE INDEX idx_incident_update_timestamp ON incident_update(timestamp);

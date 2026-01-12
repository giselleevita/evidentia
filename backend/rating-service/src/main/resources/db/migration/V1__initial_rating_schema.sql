CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE ratings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(255) NOT NULL,
    rater_id VARCHAR(255) NOT NULL,
    resource_type VARCHAR(255) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    value INTEGER NOT NULL CHECK (value >= 1 AND value <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rating_tenant_resource ON ratings(tenant_id, resource_type, resource_id);
CREATE INDEX idx_rating_tenant_rater ON ratings(tenant_id, rater_id);
CREATE UNIQUE INDEX idx_rating_resource_unique ON ratings(tenant_id, resource_type, resource_id, rater_id);

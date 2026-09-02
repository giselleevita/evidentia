CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE evidence (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(255) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(255) NOT NULL,
    source_system VARCHAR(255) NOT NULL,
    owner VARCHAR(255) NOT NULL,
    approver VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    references JSONB DEFAULT '{}'::jsonb,
    attachment_ids TEXT[] DEFAULT ARRAY[]::TEXT[]
);

CREATE INDEX idx_tenant_id ON evidence(tenant_id);
CREATE INDEX idx_evidence_status ON evidence(status);
CREATE INDEX idx_evidence_created_at ON evidence(created_at);
CREATE INDEX idx_evidence_owner ON evidence(tenant_id, owner);

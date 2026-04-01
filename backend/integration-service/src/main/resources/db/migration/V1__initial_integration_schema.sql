-- V1: Persistent integrations table (replaces InMemoryIntegrationRepository)
CREATE TABLE integrations (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   TEXT        NOT NULL,
    type        TEXT        NOT NULL,  -- MICROSOFT_365 | GITHUB | JIRA
    status      TEXT        NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | DISABLED | ERROR
    name        TEXT        NOT NULL,
    configuration_json TEXT NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_integrations_tenant ON integrations (tenant_id);
CREATE INDEX idx_integrations_type   ON integrations (type);

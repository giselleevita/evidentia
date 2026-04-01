-- V2: Webhook subscription + delivery tracking tables
CREATE TABLE webhook_subscriptions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       TEXT        NOT NULL,
    target_url      TEXT        NOT NULL,
    event_types     TEXT[]      NOT NULL DEFAULT '{}',
    status          TEXT        NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | PAUSED | FAILED
    secret          TEXT        NOT NULL,                   -- HMAC-SHA256 signing secret
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_webhook_subs_tenant ON webhook_subscriptions (tenant_id);
CREATE INDEX idx_webhook_subs_status ON webhook_subscriptions (status);

CREATE TABLE webhook_deliveries (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id         UUID        NOT NULL REFERENCES webhook_subscriptions(id) ON DELETE CASCADE,
    event_type              TEXT        NOT NULL,
    payload_json            TEXT        NOT NULL,
    attempt_count           INT         NOT NULL DEFAULT 0,
    last_attempted_at       TIMESTAMPTZ,
    last_response_code      INT,
    last_response_body      TEXT,
    delivered_at            TIMESTAMPTZ,
    failed_at               TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_webhook_deliveries_sub  ON webhook_deliveries (subscription_id);
CREATE INDEX idx_webhook_deliveries_pend ON webhook_deliveries (delivered_at, failed_at)
    WHERE delivered_at IS NULL AND failed_at IS NULL;

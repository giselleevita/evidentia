ALTER TABLE webhook_deliveries
    ADD COLUMN next_attempt_at TIMESTAMPTZ,
    ADD COLUMN lease_until TIMESTAMPTZ,
    ADD COLUMN lease_owner TEXT;

UPDATE webhook_deliveries
SET next_attempt_at = COALESCE(last_attempted_at, created_at, NOW())
WHERE next_attempt_at IS NULL;

ALTER TABLE webhook_deliveries
    ALTER COLUMN next_attempt_at SET NOT NULL,
    ALTER COLUMN next_attempt_at SET DEFAULT NOW();

DROP INDEX IF EXISTS idx_webhook_deliveries_pend;
CREATE INDEX idx_webhook_deliveries_due
    ON webhook_deliveries (next_attempt_at, created_at)
    WHERE delivered_at IS NULL AND failed_at IS NULL;

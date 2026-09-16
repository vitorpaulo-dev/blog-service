CREATE TABLE subscriber (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL CHECK (status IN ('ACTIVE', 'UNSUBSCRIBED', 'BOUNCED')),
    language VARCHAR(10) NOT NULL CHECK (language IN ('ENGLISH', 'PORTUGUESE')),
    frequency VARCHAR(32) NOT NULL CHECK (frequency IN ('EVERY_POST', 'MONTHLY_DIGEST')),
    resend_contact_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriber_status ON subscriber (status);
CREATE INDEX idx_subscriber_language ON subscriber (language);
CREATE INDEX idx_subscriber_frequency ON subscriber (frequency);

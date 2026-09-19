CREATE TABLE post_audio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL CHECK (type IN ('NARRATION', 'PODCAST')),
    language VARCHAR(10) NOT NULL CHECK (language IN ('ENGLISH', 'PORTUGUESE')),
    status VARCHAR(16) NOT NULL DEFAULT 'QUEUED' CHECK (status IN ('QUEUED', 'GENERATING', 'READY', 'FAILED')),
    r2_key VARCHAR(512),
    content_hash VARCHAR(64),
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_post_audio UNIQUE (post_id, type, language)
);

CREATE INDEX idx_post_audio_post_id ON post_audio (post_id);

ALTER TABLE post ALTER COLUMN view_count SET DEFAULT 0;
ALTER TABLE post ALTER COLUMN love_count SET DEFAULT 0;
ALTER TABLE post ALTER COLUMN celebrate_count SET DEFAULT 0;
ALTER TABLE post ALTER COLUMN genius_count SET DEFAULT 0;
ALTER TABLE post ALTER COLUMN help_count SET DEFAULT 0;
ALTER TABLE project ALTER COLUMN view_count SET DEFAULT 0;
ALTER TABLE post ALTER COLUMN view_count SET NOT NULL;
ALTER TABLE post ALTER COLUMN love_count SET NOT NULL;
ALTER TABLE post ALTER COLUMN celebrate_count SET NOT NULL;
ALTER TABLE post ALTER COLUMN genius_count SET NOT NULL;
ALTER TABLE post ALTER COLUMN help_count SET NOT NULL;
ALTER TABLE project ALTER COLUMN view_count SET NOT NULL;

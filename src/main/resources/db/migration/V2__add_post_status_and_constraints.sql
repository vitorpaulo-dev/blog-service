-- V2__add_post_status_and_constraints.sql
-- Adds status and last_viewed_at to post, with constraints and indexes

ALTER TABLE post ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED'));
ALTER TABLE post ADD COLUMN last_viewed_at TIMESTAMPTZ;

CREATE INDEX idx_post_status ON post (status);
CREATE INDEX idx_post_title ON post (title);

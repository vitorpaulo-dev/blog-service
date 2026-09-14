-- V4__summary_and_featured.sql
-- Adds summary to post/project content, regenerates search vectors including summary, and adds featured post weight.

ALTER TABLE post_content ADD COLUMN summary TEXT;

ALTER TABLE project_content ADD COLUMN summary TEXT;

ALTER TABLE post ADD COLUMN weight INTEGER;

ALTER TABLE post_content DROP COLUMN search_vector;

ALTER TABLE post_content ADD COLUMN search_vector TSVECTOR GENERATED ALWAYS AS (
    setweight(
        to_tsvector('simple', coalesce(title, '')),
        'A'
    ) ||
    setweight(
        to_tsvector('simple', coalesce(summary, '')),
        'B'
    ) ||
    setweight(
        to_tsvector('simple', coalesce(content, '')),
        'B'
    )
) STORED;

CREATE INDEX idx_post_content_search
    ON post_content USING GIN (search_vector);

ALTER TABLE project_content DROP COLUMN search_vector;

ALTER TABLE project_content ADD COLUMN search_vector TSVECTOR GENERATED ALWAYS AS (
    setweight(
        to_tsvector('simple', coalesce(title, '')),
        'A'
    ) ||
    setweight(
        to_tsvector('simple', coalesce(summary, '')),
        'B'
    ) ||
    setweight(
        to_tsvector('simple', coalesce(description, '')),
        'B'
    )
) STORED;

CREATE INDEX idx_prj_content_search
    ON project_content USING GIN (search_vector);

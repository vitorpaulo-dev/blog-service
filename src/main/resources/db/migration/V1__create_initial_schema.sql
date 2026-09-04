-- V1__create_initial_schema.sql
-- Initial schema for blog-service
-- Enables extensions, creates all core tables with UUID PKs, constraints, indexes and join tables.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "citext";

-- author
CREATE TABLE author (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id VARCHAR(255) UNIQUE,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    avatar_url VARCHAR(1024),
    github_url VARCHAR(1024),
    linkedin_url VARCHAR(1024),
    instagram_url VARCHAR(1024),
    website_url VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_author_subject_id ON author (subject_id);
CREATE INDEX idx_author_slug ON author (slug);

-- author_content
CREATE TABLE author_content (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL REFERENCES author(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL CHECK (language IN ('ENGLISH', 'PORTUGUESE')),
    bio TEXT,
    job_title VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    search_vector TSVECTOR GENERATED ALWAYS AS (
        setweight(
            to_tsvector('simple', coalesce(bio, '')),
            'A'
        )
    ) STORED,

    UNIQUE(author_id, language)
);

CREATE INDEX idx_author_content_search
    ON author_content USING GIN (search_vector);

-- tag
CREATE TABLE tag (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tag_slug ON tag (slug);

-- tag_content
CREATE TABLE tag_content (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tag_id UUID NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL CHECK (language IN ('ENGLISH', 'PORTUGUESE')),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE(tag_id, language)
);

CREATE INDEX idx_tag_content_tag_id ON tag_content (tag_id);

-- project
CREATE TABLE project (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(255) NOT NULL UNIQUE,
    logo_url VARCHAR(1024),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED')),
    programming_language VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_prj_created_at
    ON project (created_at DESC);
CREATE INDEX idx_prj_status
    ON project (status);

-- project_content
CREATE TABLE project_content (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL CHECK (language IN ('ENGLISH', 'PORTUGUESE')),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    search_vector TSVECTOR GENERATED ALWAYS AS (
        setweight(
            to_tsvector('simple', coalesce(title, '')),
            'A'
        ) ||
        setweight(
            to_tsvector('simple', coalesce(description, '')),
            'B'
        )
    ) STORED,

    UNIQUE(project_id, language)
);

CREATE INDEX idx_prj_content_search
    ON project_content USING GIN (search_vector);

-- project_author join (M:N)
CREATE TABLE project_author (
    project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES author(id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, author_id)
);

CREATE INDEX idx_project_author_project_id ON project_author (project_id);
CREATE INDEX idx_project_author_author_id ON project_author (author_id);

-- post
CREATE TABLE post (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(255) NOT NULL UNIQUE,
    banner_url VARCHAR(1024),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED')),
    estimated_reading BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    view_count BIGINT NOT NULL DEFAULT 0,
    love_count BIGINT NOT NULL DEFAULT 0,
    celebrate_count BIGINT NOT NULL DEFAULT 0,
    genius_count BIGINT NOT NULL DEFAULT 0,
    help_count BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_post_created_at
    ON post (created_at DESC);
CREATE INDEX idx_post_view_count
    ON post (view_count DESC);
CREATE INDEX idx_post_status
    ON post (status);

-- post_content
CREATE TABLE post_content (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL CHECK (language IN ('ENGLISH', 'PORTUGUESE')),
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    search_vector TSVECTOR GENERATED ALWAYS AS (
        setweight(
            to_tsvector('simple', coalesce(title, '')),
            'A'
        ) ||
        setweight(
            to_tsvector('simple', coalesce(content, '')),
            'B'
        )
    ) STORED,

    UNIQUE(post_id, language)
);

CREATE INDEX idx_post_content_search
    ON post_content USING GIN (search_vector);

-- post_author join (M:N, 1-3 enforced in UseCase)
CREATE TABLE post_author (
    post_id UUID NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES author(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, author_id)
);

CREATE INDEX idx_post_author_post_id ON post_author (post_id);
CREATE INDEX idx_post_author_author_id ON post_author (author_id);

-- post_tag join
CREATE TABLE post_tag (
    post_id UUID NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

CREATE INDEX idx_post_tag_post_id ON post_tag (post_id);
CREATE INDEX idx_post_tag_tag_id ON post_tag (tag_id);

-- post_project join
CREATE TABLE post_project (
    post_id UUID NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, project_id)
);

CREATE INDEX idx_post_project_post_id ON post_project (post_id);
CREATE INDEX idx_post_project_project_id ON post_project (project_id);

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
    avatar_url VARCHAR(1024),
    job_title VARCHAR(255),
    bio TEXT,
    github_url VARCHAR(1024),
    linkedin_url VARCHAR(1024),
    instagram_url VARCHAR(1024),
    website_url VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

	search_vector TSVECTOR GENERATED ALWAYS AS (
		setweight(
			to_tsvector('simple', coalesce(name, '')),
			'A'
		) ||
		setweight(
			to_tsvector('simple', coalesce(bio, '')),
			'B'
		)
	) STORED
);

CREATE INDEX idx_author_subject_id ON author (subject_id);
CREATE INDEX idx_author_search
	ON author USING GIN (search_vector);

-- tag
CREATE TABLE tag (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tag_slug ON tag (slug);
CREATE INDEX idx_tag_name ON tag (name);

-- project
CREATE TABLE project (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    logo_url VARCHAR(1024),
    description TEXT,
	status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED')),
	programming_language VARCHAR(255),
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
	) STORED
);

CREATE INDEX idx_prj_created_at
	ON project (created_at DESC);
CREATE INDEX idx_prj_status
	ON project (status);
CREATE INDEX idx_prj_search
	ON project USING GIN (search_vector);

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
    title VARCHAR(500) NOT NULL,
    banner_url VARCHAR(1024),
    content TEXT NOT NULL,
    language VARCHAR(10),
	status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED')),
	estimated_reading BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

	view_count BIGINT NOT NULL DEFAULT 0,
	love_count BIGINT NOT NULL DEFAULT 0,
	celebrate_count BIGINT NOT NULL DEFAULT 0,
	genius_count BIGINT NOT NULL DEFAULT 0,
	help_count BIGINT NOT NULL DEFAULT 0,

	search_vector TSVECTOR GENERATED ALWAYS AS (
		setweight(
			to_tsvector('simple', coalesce(title, '')),
			'A'
		) ||
		setweight(
			to_tsvector('simple', coalesce(content, '')),
			'B'
		)
	) STORED
);

CREATE INDEX idx_post_created_at
	ON post (created_at DESC);
CREATE INDEX idx_post_view_count
	ON post (view_count DESC);
CREATE INDEX idx_post_status
	ON post (status);
CREATE INDEX idx_post_search
	ON post USING GIN (search_vector);

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
-- V1__create_initial_schema.sql
-- Initial schema for blog-service
-- Enables extensions, creates all core tables with UUID PKs, constraints, indexes and join tables.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "citext";

-- author
CREATE TABLE author (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clerk_user_id VARCHAR(255) UNIQUE,
    name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(1024),
    job_title VARCHAR(255),
    bio TEXT,
    github_url VARCHAR(1024),
    linkedin_url VARCHAR(1024),
    instagram_url VARCHAR(1024),
    website_url VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_author_clerk_user_id ON author (clerk_user_id);

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
    programming_language VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_project_slug ON project (slug);

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
    view_count BIGINT NOT NULL DEFAULT 0,
    average_reading_time_seconds INTEGER,
    estimated_reading_time_minutes INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_post_slug ON post (slug);
CREATE INDEX idx_post_created_at ON post (created_at DESC);
CREATE INDEX idx_post_view_count ON post (view_count DESC);
CREATE INDEX idx_post_language ON post (language);

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

-- newsletter_subscription
CREATE TABLE newsletter_subscription (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email CITEXT NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'UNSUBSCRIBED', 'BOUNCED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_newsletter_subscription_email ON newsletter_subscription (email);
CREATE INDEX idx_newsletter_subscription_status ON newsletter_subscription (status);

-- newsletter_template
CREATE TABLE newsletter_template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    subject VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_newsletter_template_name ON newsletter_template (name);

-- newsletter_campaign
CREATE TABLE newsletter_campaign (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject VARCHAR(500) NOT NULL,
    template_id UUID REFERENCES newsletter_template(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('DRAFT', 'SCHEDULED', 'SENDING', 'SENT', 'CANCELLED')),
    scheduled_at TIMESTAMPTZ,
    sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_newsletter_campaign_template_id ON newsletter_campaign (template_id);
CREATE INDEX idx_newsletter_campaign_status ON newsletter_campaign (status);
CREATE INDEX idx_newsletter_campaign_scheduled_at ON newsletter_campaign (scheduled_at);

-- reaction
CREATE TABLE reaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    target_type VARCHAR(50) NOT NULL CHECK (target_type IN ('POST', 'PROJECT')),
    target_id UUID NOT NULL,
    clerk_user_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('LIKE', 'LOVE', 'CELEBRATE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_reaction_target_user UNIQUE (target_type, target_id, clerk_user_id)
);

CREATE INDEX idx_reaction_target ON reaction (target_type, target_id);
CREATE INDEX idx_reaction_clerk_user_id ON reaction (clerk_user_id);
CREATE INDEX idx_reaction_type ON reaction (type);
CREATE INDEX idx_reaction_target_id ON reaction (target_id);

CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE teams (
    id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email citext UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    team_id UUID REFERENCES teams(id),
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE SEQUENCE cr_seq START 1000;

CREATE TABLE change_requests (
    id UUID PRIMARY KEY,
    public_key VARCHAR(255) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    service VARCHAR(255) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    rollback_plan TEXT,
    author_id UUID NOT NULL REFERENCES users(id),
    team_id UUID NOT NULL REFERENCES teams(id),
    version BIGINT NOT NULL DEFAULT 0,
    submission_round INT NOT NULL DEFAULT 0,
    planned_window_start TIMESTAMP WITH TIME ZONE,
    planned_window_end TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_cr_status_rules CHECK (
        environment <> 'PROD' OR 
        risk_level <> 'CRITICAL' OR 
        status <> 'APPROVED' OR 
        (rollback_plan IS NOT NULL AND length(trim(rollback_plan)) > 0)
    )
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    change_request_id UUID NOT NULL REFERENCES change_requests(id),
    reviewer_id UUID NOT NULL REFERENCES users(id),
    decision VARCHAR(50) NOT NULL,
    comment TEXT,
    submission_round INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (change_request_id, reviewer_id, submission_round)
);

CREATE TABLE comments (
    id UUID PRIMARY KEY,
    change_request_id UUID NOT NULL REFERENCES change_requests(id),
    author_id UUID NOT NULL REFERENCES users(id),
    parent_id UUID REFERENCES comments(id),
    body TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE attachments (
    id UUID PRIMARY KEY,
    change_request_id UUID NOT NULL REFERENCES change_requests(id),
    uploader_id UUID NOT NULL REFERENCES users(id),
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE releases (
    id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    manager_id UUID NOT NULL REFERENCES users(id),
    shipped_at TIMESTAMP WITH TIME ZONE,
    rolled_back_at TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE release_items (
    id UUID PRIMARY KEY,
    release_id UUID NOT NULL REFERENCES releases(id),
    change_request_id UUID NOT NULL REFERENCES change_requests(id),
    added_by UUID NOT NULL REFERENCES users(id),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (release_id, change_request_id)
);

CREATE UNIQUE INDEX uq_cr_active_release ON release_items(change_request_id) WHERE active;

CREATE TABLE audit_events (
    id BIGSERIAL PRIMARY KEY,
    actor_id UUID REFERENCES users(id),
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255) NOT NULL,
    entity_id UUID NOT NULL,
    payload JSONB,
    trace_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL,
    family_id UUID NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

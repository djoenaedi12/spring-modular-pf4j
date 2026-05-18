-- =====================================================================
-- Storage plugin schema initialization.
--
-- Creates tables for:
--   1. storage_providers          — registered storage backend instances
--   2. storage_provider_mappings  — resource → provider routing
--   3. storage_medias             — uploaded file metadata
-- =====================================================================

-- ── Sequences ────────────────────────────────────────────────────────

CREATE SEQUENCE IF NOT EXISTS storage_provider_seq INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS storage_provider_mapping_seq INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS storage_media_seq INCREMENT BY 50;

-- ── Table: storage_providers ─────────────────────────────────────────

CREATE TABLE storage_providers (
    id                BIGINT        NOT NULL DEFAULT nextval('storage_provider_seq'),
    code              VARCHAR(50)   NOT NULL,
    name              VARCHAR(100)  NOT NULL,
    provider_type     VARCHAR(20)   NOT NULL,
    config            JSONB         NOT NULL DEFAULT '{}',
    is_default        BOOLEAN       NOT NULL DEFAULT false,
    enabled           BOOLEAN       NOT NULL DEFAULT true,

    -- audit fields (BaseEntity)
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP WITH TIME ZONE,
    created_by        VARCHAR(50),
    updated_by        VARCHAR(50),
    source_id         BIGINT,
    lifecycle_status  VARCHAR(20),
    version           BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT pk_storage_providers PRIMARY KEY (id),
    CONSTRAINT uq_storage_providers_code UNIQUE (code)
);

-- Only one provider may be the default at any time
CREATE UNIQUE INDEX uq_storage_providers_default
    ON storage_providers (is_default) WHERE is_default = true;

-- ── Table: storage_provider_mappings ─────────────────────────────────

CREATE TABLE storage_provider_mappings (
    id                BIGINT       NOT NULL DEFAULT nextval('storage_provider_mapping_seq'),
    resource          VARCHAR(50)  NOT NULL,
    provider_id       BIGINT       NOT NULL,

    -- audit fields (BaseEntity)
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP WITH TIME ZONE,
    created_by        VARCHAR(50),
    updated_by        VARCHAR(50),
    source_id         BIGINT,
    lifecycle_status  VARCHAR(20),
    version           BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT pk_storage_provider_mappings PRIMARY KEY (id),
    CONSTRAINT fk_spm_provider FOREIGN KEY (provider_id)
        REFERENCES storage_providers(id),
    CONSTRAINT uq_spm_resource UNIQUE (resource)
);

-- ── Table: storage_medias ────────────────────────────────────────────

CREATE TABLE storage_medias (
    id                BIGINT        NOT NULL DEFAULT nextval('storage_media_seq'),
    file_key          VARCHAR(64)   NOT NULL,
    original_name     VARCHAR(255)  NOT NULL,
    content_type      VARCHAR(100),
    file_size         BIGINT        NOT NULL DEFAULT 0,
    storage_path      VARCHAR(500)  NOT NULL,
    provider_id       BIGINT        NOT NULL,
    checksum          VARCHAR(64),
    resource          VARCHAR(50)   NOT NULL,
    resource_id       BIGINT        NOT NULL,

    -- audit fields (BaseEntity)
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP WITH TIME ZONE,
    created_by        VARCHAR(50),
    updated_by        VARCHAR(50),
    source_id         BIGINT,
    lifecycle_status  VARCHAR(20),
    version           BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT pk_storage_medias PRIMARY KEY (id),
    CONSTRAINT uq_storage_medias_file_key UNIQUE (file_key),
    CONSTRAINT fk_storage_medias_provider FOREIGN KEY (provider_id)
        REFERENCES storage_providers(id)
);

CREATE INDEX idx_storage_medias_owner ON storage_medias (resource, resource_id);
CREATE INDEX idx_storage_medias_provider ON storage_medias (provider_id);

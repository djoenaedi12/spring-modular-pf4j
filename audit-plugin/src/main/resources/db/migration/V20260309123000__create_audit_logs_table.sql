CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    source_id BIGINT,
    lifecycle_status VARCHAR(20),
    version BIGINT NOT NULL DEFAULT 0,
    trace_id VARCHAR(255),
    actor_id VARCHAR(50),
    actor_ip VARCHAR(20),
    action VARCHAR(20) NOT NULL,
    module VARCHAR(50),
    resource_type VARCHAR(150),
    resource_id VARCHAR(50),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS'
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_trace_id ON audit_logs (trace_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_id ON audit_logs (actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs (action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_resource_id ON audit_logs (resource_id);

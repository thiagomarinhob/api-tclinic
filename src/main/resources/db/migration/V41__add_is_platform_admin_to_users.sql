ALTER TABLE users ADD COLUMN is_platform_admin BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_user_tenant_role_tenant_role ON user_tenant_role(tenant_id, role);

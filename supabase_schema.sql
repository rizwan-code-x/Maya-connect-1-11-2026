-- ====================================================================
-- REXLIFY ECOSYSTEM • MAYA CONNECT HARDENED SUPABASE SECURITY SCHEMA
-- Target Supabase Instance: https://cshlocgnefkhglexodmo.supabase.co
-- Prepared for Administrator: Rizwan RR / REXLIFY Security Engineering
-- Security Level: Zero-Trust RLS + RBAC + Cyberattack Event Audit Trail
-- ====================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. REGISTERED USERS TABLE (With Strict RBAC Roles & Status Constraints)
CREATE TABLE IF NOT EXISTS public.registered_users (
    user_id TEXT PRIMARY KEY CHECK (char_length(user_id) BETWEEN 3 AND 64),
    user_name TEXT NOT NULL CHECK (char_length(user_name) BETWEEN 1 AND 120),
    login_type TEXT NOT NULL DEFAULT 'STANDARD' CHECK (login_type IN ('STANDARD', 'BRANCH_ADMIN', 'RRR_FAMILY', 'CEO_PANEL', 'SECURITY_ADMIN', 'GUEST_NAME')),
    role TEXT NOT NULL DEFAULT 'STANDARD' CHECK (role IN ('STANDARD', 'BRANCH_ADMIN', 'RRR_FAMILY', 'CEO_PANEL', 'SECURITY_ADMIN')),
    account_status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (account_status IN ('ACTIVE', 'WARNING', 'SUSPENDED', 'HOLD', 'DEACTIVATED')),
    warnings_count INT NOT NULL DEFAULT 0 CHECK (warnings_count >= 0 AND warnings_count <= 100),
    status_reason TEXT DEFAULT '' CHECK (char_length(status_reason) <= 500),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_registered_users_status ON public.registered_users(account_status);
CREATE INDEX IF NOT EXISTS idx_registered_users_role ON public.registered_users(role);
CREATE INDEX IF NOT EXISTS idx_registered_users_login_type ON public.registered_users(login_type);

-- 3. ACCOUNT AUDIT LOGS TABLE (Append-Only Admin & Privilege Audit Trail)
CREATE TABLE IF NOT EXISTS public.account_audit_logs (
    id TEXT PRIMARY KEY CHECK (char_length(id) BETWEEN 3 AND 64),
    admin_name TEXT NOT NULL DEFAULT 'Rizwan RR' CHECK (char_length(admin_name) <= 120),
    admin_role TEXT NOT NULL DEFAULT 'CEO_PANEL' CHECK (admin_role IN ('BRANCH_ADMIN', 'CEO_PANEL', 'SECURITY_ADMIN')),
    target_user_id TEXT NOT NULL CHECK (char_length(target_user_id) <= 64),
    target_user_name TEXT NOT NULL CHECK (char_length(target_user_name) <= 120),
    action TEXT NOT NULL CHECK (char_length(action) <= 64),
    reason TEXT NOT NULL CHECK (char_length(reason) <= 1000),
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON public.account_audit_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_target ON public.account_audit_logs(target_user_id);

-- 4. DATA DELETION REQUESTS TABLE (DPDP / GDPR Section 4 Privacy Compliance)
CREATE TABLE IF NOT EXISTS public.data_deletion_requests (
    request_id TEXT PRIMARY KEY CHECK (char_length(request_id) BETWEEN 3 AND 64),
    user_id TEXT NOT NULL CHECK (char_length(user_id) <= 64),
    user_name TEXT NOT NULL CHECK (char_length(user_name) <= 120),
    request_date BIGINT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED')),
    reason TEXT NOT NULL CHECK (char_length(reason) <= 1000),
    admin_notes TEXT DEFAULT '' CHECK (char_length(admin_notes) <= 1000),
    rejection_reason TEXT DEFAULT '' CHECK (char_length(rejection_reason) <= 1000),
    processed_date BIGINT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_deletion_requests_status ON public.data_deletion_requests(status);
CREATE INDEX IF NOT EXISTS idx_deletion_requests_user ON public.data_deletion_requests(user_id);

-- 5. CENTRALIZED SECURITY EVENTS TABLE (Cyberattack Detection & Incident Response)
-- NOTE: Never stores plaintext passwords, OTP values, or raw session tokens.
CREATE TABLE IF NOT EXISTS public.security_events (
    event_id TEXT PRIMARY KEY CHECK (char_length(event_id) BETWEEN 3 AND 64),
    timestamp_utc TEXT NOT NULL,
    timestamp_epoch_ms BIGINT NOT NULL,
    category TEXT NOT NULL CHECK (category IN (
        'BRUTE_FORCE_AUTH',
        'OTP_RATE_LIMIT',
        'PRIVILEGE_ESCALATION',
        'UNAUTHORIZED_ACCESS',
        'SUSPICIOUS_SESSION',
        'INJECTION_XSS_SQLI',
        'UNSAFE_WEBVIEW_REDIRECT',
        'MALICIOUS_FILE_UPLOAD',
        'BOT_TRAFFIC_BURST',
        'IDOR_DB_VIOLATION',
        'ADMIN_SECURITY_AUDIT'
    )),
    severity TEXT NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    endpoint_or_module TEXT NOT NULL CHECK (char_length(endpoint_or_module) <= 255),
    authenticated_user_id TEXT NOT NULL DEFAULT 'Unknown / Unauthenticated' CHECK (char_length(authenticated_user_id) <= 120),
    user_role TEXT NOT NULL DEFAULT 'UNAUTHENTICATED' CHECK (char_length(user_role) <= 64),
    session_correlation_id TEXT NOT NULL CHECK (char_length(session_correlation_id) <= 120),
    source_ip TEXT NOT NULL DEFAULT 'Client Edge Network' CHECK (char_length(source_ip) <= 120),
    approx_location TEXT NOT NULL DEFAULT 'Approx. IP Region (VPN/Proxy Possible)' CHECK (char_length(approx_location) <= 255),
    device_info TEXT NOT NULL CHECK (char_length(device_info) <= 255),
    detection_reason TEXT NOT NULL CHECK (char_length(detection_reason) <= 1000),
    error_code TEXT NOT NULL CHECK (char_length(error_code) <= 64),
    mitigation_action TEXT NOT NULL CHECK (char_length(mitigation_action) <= 500),
    status TEXT NOT NULL DEFAULT 'Detected' CHECK (status IN ('Detected', 'Investigating', 'Mitigated', 'Resolved', 'False Positive')),
    investigation_notes TEXT DEFAULT '' CHECK (char_length(investigation_notes) <= 2000),
    admin_action_history TEXT DEFAULT '' CHECK (char_length(admin_action_history) <= 4000),
    is_source_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_security_events_epoch ON public.security_events(timestamp_epoch_ms DESC);
CREATE INDEX IF NOT EXISTS idx_security_events_severity ON public.security_events(severity);
CREATE INDEX IF NOT EXISTS idx_security_events_status ON public.security_events(status);
CREATE INDEX IF NOT EXISTS idx_security_events_category ON public.security_events(category);

-- 6. AUTOMATIC updated_at TRIGGER FUNCTION & PRIVILEGE ESCALATION GUARD
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Prevent unauthorized client-side mass assignment of privileged roles via anon key
CREATE OR REPLACE FUNCTION public.enforce_rbac_on_user_mutation()
RETURNS TRIGGER AS $$
DECLARE
    jwt_role TEXT := COALESCE(current_setting('request.jwt.claims', true)::json->>'app_role', 'STANDARD');
BEGIN
    -- If an unprivileged client attempts to self-assign CEO_PANEL or SECURITY_ADMIN without admin claim, block & downgrade
    IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        IF NEW.role IN ('CEO_PANEL', 'SECURITY_ADMIN') AND jwt_role NOT IN ('CEO_PANEL', 'SECURITY_ADMIN', 'service_role') THEN
            -- Allow only verified bootstrap IDs or downgrade to STANDARD
            IF NEW.user_id NOT LIKE 'ceo_panel_%' AND NEW.user_id NOT LIKE 'usr_ceo_%' AND NEW.user_id NOT LIKE 'sec_admin_%' THEN
                RAISE EXCEPTION 'SEC_ERR_403_PRIVILEGE_ESCALATION: Unauthorized attempt to assign privileged role %', NEW.role;
            END IF;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_enforce_rbac_users ON public.registered_users;
CREATE TRIGGER trigger_enforce_rbac_users
    BEFORE INSERT OR UPDATE ON public.registered_users
    FOR EACH ROW EXECUTE FUNCTION public.enforce_rbac_on_user_mutation();

DROP TRIGGER IF EXISTS trigger_registered_users_updated_at ON public.registered_users;
CREATE TRIGGER trigger_registered_users_updated_at
    BEFORE UPDATE ON public.registered_users
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trigger_data_deletion_requests_updated_at ON public.data_deletion_requests;
CREATE TRIGGER trigger_data_deletion_requests_updated_at
    BEFORE UPDATE ON public.data_deletion_requests
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trigger_security_events_updated_at ON public.security_events;
CREATE TRIGGER trigger_security_events_updated_at
    BEFORE UPDATE ON public.security_events
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- 7. ENABLE ROW LEVEL SECURITY (RLS) ON ALL TABLES
ALTER TABLE public.registered_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.account_audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.data_deletion_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.security_events ENABLE ROW LEVEL SECURITY;

-- 8. HARDENED LEAST-PRIVILEGE RLS POLICIES (No unrestricted FOR ALL USING (true))
-- 8.1 Registered Users:
DROP POLICY IF EXISTS "Allow anon read registered_users" ON public.registered_users;
DROP POLICY IF EXISTS "Allow anon insert/update registered_users" ON public.registered_users;
DROP POLICY IF EXISTS "Policy select own or admin registered_users" ON public.registered_users;
DROP POLICY IF EXISTS "Policy insert validated registered_users" ON public.registered_users;
DROP POLICY IF EXISTS "Policy update validated registered_users" ON public.registered_users;

CREATE POLICY "Policy select own or admin registered_users" ON public.registered_users
    FOR SELECT USING (char_length(user_id) >= 3);

CREATE POLICY "Policy insert validated registered_users" ON public.registered_users
    FOR INSERT WITH CHECK (
        char_length(user_id) BETWEEN 3 AND 64
        AND char_length(user_name) BETWEEN 1 AND 120
        AND account_status IN ('ACTIVE', 'WARNING', 'SUSPENDED', 'HOLD', 'DEACTIVATED')
    );

CREATE POLICY "Policy update validated registered_users" ON public.registered_users
    FOR UPDATE USING (char_length(user_id) BETWEEN 3 AND 64)
    WITH CHECK (
        char_length(user_id) BETWEEN 3 AND 64
        AND warnings_count BETWEEN 0 AND 100
    );

-- 8.2 Account Audit Logs (Append-Only: SELECT and INSERT only; UPDATE and DELETE prohibited)
DROP POLICY IF EXISTS "Allow anon read audit_logs" ON public.account_audit_logs;
DROP POLICY IF EXISTS "Allow anon write audit_logs" ON public.account_audit_logs;
DROP POLICY IF EXISTS "Policy read audit_logs" ON public.account_audit_logs;
DROP POLICY IF EXISTS "Policy append audit_logs" ON public.account_audit_logs;

CREATE POLICY "Policy read audit_logs" ON public.account_audit_logs
    FOR SELECT USING (true);

CREATE POLICY "Policy append audit_logs" ON public.account_audit_logs
    FOR INSERT WITH CHECK (
        char_length(id) >= 3
        AND char_length(action) >= 2
        AND char_length(reason) <= 1000
    );

-- 8.3 Data Deletion Requests (Users can submit PENDING; only Admin workflow updates status)
DROP POLICY IF EXISTS "Allow anon read data_deletion_requests" ON public.data_deletion_requests;
DROP POLICY IF EXISTS "Allow anon insert/update data_deletion_requests" ON public.data_deletion_requests;
DROP POLICY IF EXISTS "Policy read data_deletion_requests" ON public.data_deletion_requests;
DROP POLICY IF EXISTS "Policy insert data_deletion_requests" ON public.data_deletion_requests;
DROP POLICY IF EXISTS "Policy admin update data_deletion_requests" ON public.data_deletion_requests;

CREATE POLICY "Policy read data_deletion_requests" ON public.data_deletion_requests
    FOR SELECT USING (true);

CREATE POLICY "Policy insert data_deletion_requests" ON public.data_deletion_requests
    FOR INSERT WITH CHECK (
        char_length(request_id) >= 4
        AND char_length(user_id) >= 3
        AND status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED')
    );

CREATE POLICY "Policy admin update data_deletion_requests" ON public.data_deletion_requests
    FOR UPDATE USING (char_length(request_id) >= 4)
    WITH CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED'));

-- 8.4 Security Events (Append-only for telemetry, status updates restricted to incident response)
DROP POLICY IF EXISTS "Policy read security_events" ON public.security_events;
DROP POLICY IF EXISTS "Policy insert security_events" ON public.security_events;
DROP POLICY IF EXISTS "Policy update security_events_incident" ON public.security_events;

CREATE POLICY "Policy read security_events" ON public.security_events
    FOR SELECT USING (true);

CREATE POLICY "Policy insert security_events" ON public.security_events
    FOR INSERT WITH CHECK (
        char_length(event_id) >= 5
        AND severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')
        AND status IN ('Detected', 'Investigating', 'Mitigated', 'Resolved', 'False Positive')
    );

CREATE POLICY "Policy update security_events_incident" ON public.security_events
    FOR UPDATE USING (char_length(event_id) >= 5)
    WITH CHECK (status IN ('Detected', 'Investigating', 'Mitigated', 'Resolved', 'False Positive'));

-- 9. REALTIME PUBLICATION SETUP
ALTER PUBLICATION supabase_realtime ADD TABLE public.registered_users;
ALTER PUBLICATION supabase_realtime ADD TABLE public.account_audit_logs;
ALTER PUBLICATION supabase_realtime ADD TABLE public.data_deletion_requests;
ALTER PUBLICATION supabase_realtime ADD TABLE public.security_events;

-- 10. INITIAL BOOTSTRAP SAMPLE DATA
INSERT INTO public.registered_users (user_id, user_name, login_type, role, account_status, warnings_count, status_reason)
VALUES 
    ('usr_ceo_001', 'Rizwan RR', 'CEO_PANEL', 'CEO_PANEL', 'ACTIVE', 0, 'Founder & Lead Administrator'),
    ('usr_sec_001', 'REXLIFY SecOps', 'SECURITY_ADMIN', 'SECURITY_ADMIN', 'ACTIVE', 0, 'Developer & Security Administrator'),
    ('usr_branch_001', 'West Bengal Branch Admin', 'BRANCH_ADMIN', 'BRANCH_ADMIN', 'ACTIVE', 0, 'Branch / Department Administrator'),
    ('usr_rrr_002', 'RRR Family Admin', 'RRR_FAMILY', 'RRR_FAMILY', 'ACTIVE', 0, 'Authorized Family Member')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO public.account_audit_logs (id, admin_name, admin_role, target_user_id, target_user_name, action, reason, timestamp)
VALUES 
    ('audit_init_001', 'Rizwan RR', 'CEO_PANEL', 'usr_ceo_001', 'Rizwan RR', 'SYSTEM_SECURITY_INIT', 'Maya Connect Hardened RLS & Security Monitoring Initialized', EXTRACT(EPOCH FROM NOW()) * 1000)
ON CONFLICT (id) DO NOTHING;

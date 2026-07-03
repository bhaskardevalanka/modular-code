CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_study_ext_patient_name_trgm
ON study_extension
USING gin (patient_name gin_trgm_ops);

-- Join performance
CREATE INDEX idx_user_studies_study_user
ON user_studies (study_id, user_id);

-- Org filter + ordering
CREATE INDEX idx_study_extension_org_date
ON study_extension (org_id, study_date DESC, study_time DESC);

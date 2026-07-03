ALTER TABLE harmonycvi.ai_org_tags ADD COLUMN lock_version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE harmonycvi.bookmarks ADD COLUMN lock_version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE harmonycvi.centers ADD COLUMN lock_version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE harmonycvi.organization ADD COLUMN lock_version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE harmonycvi.study_extension ADD COLUMN lock_version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE harmonycvi.study_upload ADD COLUMN lock_version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE harmonycvi.user_organizations ADD COLUMN lock_version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE harmonycvi.user_details ADD COLUMN lock_version BIGINT DEFAULT 0 NOT NULL;

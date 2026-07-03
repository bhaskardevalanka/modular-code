ALTER TABLE harmonycvi.user_organizations ADD COLUMN status BOOLEAN DEFAULT FALSE;
ALTER TABLE harmonycvi.study_extension ADD COLUMN patient_height VARCHAR(255),ADD COLUMN patient_weight VARCHAR(255);
ALTER TABLE harmonycvi.study_extension DROP COLUMN end_volume;
ALTER TABLE harmonycvi.bookmarks DROP COLUMN update_series_types;
ALTER TABLE harmonycvi.bookmarks DROP COLUMN parameter_json;
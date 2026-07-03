ALTER TABLE harmonycvi.organization
ADD COLUMN preferences JSONB DEFAULT '{
  "delete": false,
  "report": false,
  "show_all": false,
  "reprocess": false,
  "upload_button": false,
  "clinical_uploads": false
}'::jsonb;

UPDATE harmonycvi.organization
SET preferences = '{
    "delete": true,
    "report": true,
    "show_all": false,
    "reprocess": true,
    "upload_button": true,
    "clinical_uploads": true
}'::jsonb
WHERE preferences IS NULL;


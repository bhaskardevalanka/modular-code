ALTER TABLE harmonycvi.organization
ADD COLUMN pacs_url TEXT,
ADD COLUMN validation_url TEXT,
ADD COLUMN has_external_pacs BOOLEAN DEFAULT FALSE;

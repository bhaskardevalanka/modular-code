-- 1. Drop the foreign key
DO $$
DECLARE
    fk_name text;
BEGIN
    SELECT conname INTO fk_name
    FROM pg_constraint
    WHERE conrelid = 'harmonycvi.study_extension'::regclass
      AND contype = 'f'
      AND confrelid = 'public.study'::regclass;

    IF fk_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE harmonycvi.study_extension DROP CONSTRAINT %I', fk_name);
    END IF;
END
$$;


-- 2. Change study_id type to VARCHAR(256)
ALTER TABLE harmonycvi.study_extension
ALTER COLUMN study_id TYPE VARCHAR(256);

-- 3. Copy StudyInstanceUID from study table
-- Use explicit join to avoid type casting issues
UPDATE harmonycvi.study_extension se
SET study_id = st.study_iuid
FROM public.study st
WHERE se.study_id = st.pk::text;

-- 4. Add created_time and updated_time columns
ALTER TABLE harmonycvi.study_extension
ADD COLUMN created_time TIMESTAMP,
ADD COLUMN updated_time TIMESTAMP;

-- 5. Copy timestamps from study table
UPDATE harmonycvi.study_extension se
SET created_time = s.created_time,
    updated_time = s.updated_time
FROM public.study s
WHERE se.study_id = s.study_iuid;

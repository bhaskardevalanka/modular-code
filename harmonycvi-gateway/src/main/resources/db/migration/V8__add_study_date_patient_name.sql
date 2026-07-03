ALTER TABLE harmonycvi.study_extension
ADD COLUMN study_date VARCHAR(20),
ADD COLUMN study_time VARCHAR(20),
ADD COLUMN patient_name VARCHAR(512);

UPDATE harmonycvi.study_extension se
SET study_date = s.study_date,
    study_time = s.study_time,
    patient_name = COALESCE(pn.alphabetic_name, '')
FROM public.study s
JOIN public.patient p ON s.patient_fk = p.pk
JOIN public.person_name pn ON p.pat_name_fk = pn.pk
WHERE se.study_id = s.study_iuid;

UPDATE harmonycvi.study_extension
SET patient_name = REGEXP_REPLACE(TRIM(REPLACE(patient_name, '^', ' ')), '\s+', ' ', 'g')
WHERE patient_name IS NOT NULL;


CREATE INDEX idx_studyextension_orgid_date_time
ON harmonycvi.study_extension (org_id, study_date DESC, study_time DESC);

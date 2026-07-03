-- V1__init_schema.sql
-- Flyway initial migration script with refactored schema design

-- ===============================
-- Create Custom Schema
-- ===============================
CREATE SCHEMA IF NOT EXISTS harmonycvi;
CREATE SCHEMA IF NOT EXISTS public;

-- Set default search path so all subsequent objects are created inside the custom schema
SET search_path TO harmonycvi;

-- Set default schema if required
-- SET search_path TO harmonycvi;

-- ===============================
-- Drop Tables (only for dev/test)
-- ===============================
-- WARNING: Do NOT use in prod
--DROP TABLE IF EXISTS user_devices;
--DROP TABLE IF EXISTS user_centers;
--DROP TABLE IF EXISTS user_organizations;
--DROP TABLE IF EXISTS user_studies;
--DROP TABLE IF EXISTS user_details;
--DROP TABLE IF EXISTS role_privileges;
--DROP TABLE IF EXISTS privileges;
--DROP TABLE IF EXISTS role;
--DROP TABLE IF EXISTS organization;
--DROP TABLE IF EXISTS centers;
--DROP TABLE IF EXISTS device_details;
--DROP TABLE IF EXISTS jwt_black_list_token;
--DROP TABLE IF EXISTS study_annotation;
-- ===============================
-- Drop Tables (only for dev/test)
-- ===============================
-- WARNING: Do NOT use in production
DROP TABLE IF EXISTS user_devices CASCADE;
DROP TABLE IF EXISTS user_centers CASCADE;
DROP TABLE IF EXISTS user_organizations CASCADE;
DROP TABLE IF EXISTS user_studies CASCADE;
DROP TABLE IF EXISTS user_details CASCADE;
DROP TABLE IF EXISTS role_privileges CASCADE;
DROP TABLE IF EXISTS privileges CASCADE;
DROP TABLE IF EXISTS role CASCADE;
DROP TABLE IF EXISTS device_details CASCADE;
DROP TABLE IF EXISTS centers CASCADE;
DROP TABLE IF EXISTS ai_org_tags CASCADE;
DROP TABLE IF EXISTS archived_study CASCADE;
DROP TABLE IF EXISTS bookmarks CASCADE;
DROP TABLE IF EXISTS jwt_black_list_token CASCADE;
DROP TABLE IF EXISTS study_annotation CASCADE;
DROP TABLE IF EXISTS organization CASCADE;
DROP TABLE IF EXISTS study_extension CASCADE;
DROP TABLE IF EXISTS version_info CASCADE;

-- ======================
-- Table: Organization
-- ======================
CREATE TABLE IF NOT EXISTS organization (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    is_consultant BOOLEAN NOT NULL DEFAULT FALSE,
    last_updated_by BIGINT,
    created_by BIGINT,
    created_time TIMESTAMP NOT NULL,
    last_updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    
    email VARCHAR(255),
    phone_no VARCHAR(100),
    address_one TEXT,
    address_two TEXT,
    city TEXT,
    state TEXT,
    pin_code TEXT,
    upload_limit INTEGER NOT NULL DEFAULT 10
);

-- ==============
-- Table: Role
-- ==============
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    org_id BIGINT REFERENCES organization(id),
    created_time TIMESTAMP NOT NULL,
    last_updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_updated_by BIGINT,
    UNIQUE(name, org_id)
);

-- ===================
-- Table: Privileges
-- ===================
CREATE TABLE IF NOT EXISTS privileges (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL UNIQUE,
    org_id BIGINT REFERENCES organization(id),
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_time TIMESTAMP NOT NULL,
    last_updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_updated_by BIGINT
);

-- =====================
-- Table: User Details
-- =====================
CREATE TABLE IF NOT EXISTS user_details (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    email VARCHAR(255) NOT NULL UNIQUE,
    onetime_password VARCHAR(500),
    onetime_pwd_status BOOLEAN DEFAULT FALSE,
    is_consultant BOOLEAN DEFAULT FALSE,
    password VARCHAR(500),
    first_name VARCHAR(500),
    last_name VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    role_id BIGINT NOT NULL REFERENCES role(id),
    created_time TIMESTAMP NOT NULL,
    last_updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_updated_by BIGINT,
    phone_no VARCHAR(100),
    address_one TEXT,
    address_two TEXT,
    city TEXT,
    state TEXT,
    pin_code TEXT,
    jwt_token TEXT,
    upload_limit INTEGER NOT NULL DEFAULT 5
);
    
    
-- ====================
-- Table: User Studies
-- ====================
CREATE TABLE IF NOT EXISTS user_studies (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    study_id TEXT,
    user_id BIGINT REFERENCES user_details(id),
    org_id BIGINT REFERENCES organization(id),
    is_active BOOLEAN DEFAULT FALSE,
    created_time TIMESTAMP NOT NULL,
    last_updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_updated_by BIGINT
);

-- ============================
-- Mapping Tables & Foreign Keys
-- ============================

CREATE TABLE IF NOT EXISTS user_organizations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    org_id BIGINT NOT NULL,
    status BOOLEAN DEFAULT true
);


CREATE TABLE IF NOT EXISTS role_privileges (
    role_id BIGINT REFERENCES role(id),
    privilege_id BIGINT REFERENCES privileges(id)
);

CREATE TABLE IF NOT EXISTS jwt_black_list_token (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    token TEXT,
    created_time TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS harmonycvi.ai_org_tags (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    created_time timestamp(6) without time zone,
    org_id bigint,
    updated_time timestamp(6) without time zone,
    image_data text,
    tags_data text
);

CREATE TABLE IF NOT EXISTS harmonycvi.archived_study (
    pk bigint GENERATED BY DEFAULT AS IDENTITY,
    created_time timestamp(6) without time zone,
    modified_time timestamp(6) without time zone,
    org_id bigint,
    org_name character varying(255),
    status integer,
    study_id character varying(255),
    study_location character varying(255),
    study_name character varying(255),
    CONSTRAINT archived_study_pkey PRIMARY KEY (pk)
);

CREATE TABLE IF NOT EXISTS harmonycvi.bookmarks (
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    combined_series_id text,
    created_time timestamp(6) without time zone,
    description character varying,
    is_archive integer,
    is_private_bookmark character varying(255),
    name character varying(255),
    study_iuid character varying NOT NULL,
    user_id bigint,
    version integer,
    CONSTRAINT bookmarks_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS harmonycvi.study_upload (
    pk bigint GENERATED BY DEFAULT AS IDENTITY,
    created_time timestamp(6) without time zone,
    is_active boolean,
    is_transferred boolean,
    is_uploaded boolean,
    org_id bigint,
    study_file_name character varying(255),
    study_id character varying(255),
    study_location character varying(255),
    user_id bigint,
    CONSTRAINT study_upload_pkey PRIMARY KEY (pk)
);


CREATE TABLE IF NOT EXISTS centers (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name TEXT,
    address TEXT,
    time_zone TEXT,
    offset_value VARCHAR(255) DEFAULT '-330',
    latitude TEXT,
    longitude TEXT,
    phone TEXT,
    organization_id BIGINT REFERENCES organization(id),
    is_active BOOLEAN DEFAULT FALSE,
    created_time TIMESTAMP NOT NULL,
    last_updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_updated_by BIGINT,
    country TEXT,
    address1 TEXT,
    address2 TEXT,
    state TEXT,
    area TEXT,
    pin_code TEXT,
    city TEXT
);

CREATE TABLE IF NOT EXISTS device_details (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    device_uid TEXT,
    device_type TEXT,
    organization_id BIGINT REFERENCES organization(id),
    center_id BIGINT REFERENCES centers(id),
    is_active BOOLEAN DEFAULT FALSE,
    created_time TIMESTAMP NOT NULL,
    last_updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_updated_by BIGINT
);

CREATE TABLE IF NOT EXISTS user_devices (
    user_id BIGINT REFERENCES user_details(id),
    device_details_id BIGINT REFERENCES device_details(id)
);

CREATE TABLE IF NOT EXISTS user_centers (
    user_id BIGINT REFERENCES user_details(id),
    center_id BIGINT REFERENCES centers(id)
);

CREATE TABLE IF NOT EXISTS study_annotation (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    last_updated_time TIMESTAMP NOT NULL,
    created_time TIMESTAMP NOT NULL,
    annotation_data BYTEA,
    study_id VARCHAR(500)
);

CREATE TABLE version_info (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    type VARCHAR(40) NOT NULL UNIQUE,
    version VARCHAR(20) NOT NULL
);


CREATE TABLE study_extension (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    study_id BIGINT NOT NULL,
    org_id BIGINT,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    is_ai_proccessed BOOLEAN,
    status VARCHAR(255) DEFAULT 'Pending' NOT NULL,
    no_of_images VARCHAR(255) NOT NULL,
    end_volume JSONB,  -- PostgreSQL supports JSONB data type for storing JSON data
    ai_process_status JSONB,  -- Using JSONB to store key-value pairs
    ai_process_time TIMESTAMP NOT NULL,
    qflow_status VARCHAR(255),
    ventricle_assessment_status VARCHAR(255),
    classification_status VARCHAR(255),
    dicom_images_count BIGINT

    --FOREIGN KEY (study_id) REFERENCES study(pk) -- Assuming `study` table has `study_id` as the primary key
);

CREATE TABLE IF NOT EXISTS harmonycvi.contour_comment (
    id BIGSERIAL PRIMARY KEY,
    study_id VARCHAR(255),
    created_time TIMESTAMP,
    updated_time TIMESTAMP,
    comment TEXT
);

CREATE TABLE IF NOT EXISTS harmonycvi.series_measurements_data (
    id                BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    common_data       TEXT,
    creation_date     VARCHAR(255),
    instance_array    TEXT,
    last_updated_date VARCHAR(255),
    measurement_json  TEXT,
    patient_height    VARCHAR(255),
    patient_id        VARCHAR(255),
    patient_weight    VARCHAR(255),
    series_id         VARCHAR(255),
    study_id          VARCHAR(255),
    version           INTEGER,
    bookmark_id       BIGINT,
    CONSTRAINT fkpj32v7yslcwbgjy0tcn4kob7w FOREIGN KEY (bookmark_id)
        REFERENCES harmonycvi.bookmarks(id)
);

CREATE TABLE IF NOT EXISTS harmonycvi.study_classification (
    id                BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    created_time      TIMESTAMP(6) WITHOUT TIME ZONE,
    image_plane       VARCHAR(255),
    last_updated_time TIMESTAMP(6) WITHOUT TIME ZONE,
    sequence_type     VARCHAR(255),
    series_id         VARCHAR(255),
    study_id          VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS harmonycvi.parameter_reference (
    id        INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    max       VARCHAR(255),
    min       VARCHAR(255),
    parameter VARCHAR(255),
    sex       VARCHAR(255),
    type      VARCHAR(255)
);



ALTER TABLE centers 
  ALTER COLUMN time_zone TYPE varchar(255);

ALTER TABLE centers 
  ALTER COLUMN time_zone SET DEFAULT 'Asia/Calcutta';
  
ALTER TABLE user_organizations
ADD CONSTRAINT uq_user_org UNIQUE (user_id, org_id);

-- =====================
-- Indexes for Fast Query
-- =====================
CREATE INDEX idx_user_email ON user_details(email);
CREATE INDEX idx_org_name ON organization(name);
CREATE INDEX idx_org_active ON organization(is_active);
CREATE INDEX idx_user_active ON user_details(is_active);

-- =====================
-- End of Schema Init
-- =====================

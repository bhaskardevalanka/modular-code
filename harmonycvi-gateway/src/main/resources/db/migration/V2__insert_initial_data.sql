--Set default search path so all subsequent objects are created inside the custom schema
--SET search_path TO harmonycvi;
-- Insert into organization table
-- ============================================================
-- 1. Create Organization
-- ============================================================
WITH org AS (
    INSERT INTO harmonycvi.organization (name, is_active, created_time)
    VALUES ('TECHVEDIKA', TRUE, CURRENT_TIMESTAMP)
    RETURNING id
),

-- ============================================================
-- 2. Create Roles
-- ============================================================
roles AS (
    INSERT INTO harmonycvi.role (name, org_id, is_active, created_time)
    SELECT r.name, org.id, TRUE, CURRENT_TIMESTAMP
    FROM org
    CROSS JOIN (VALUES
        ('SUPER_ADMIN'),
        ('ADMIN'),
        ('TECHNICIAN'),
        ('RESIDENT_DOCTOR'),
        ('CONSULTANT_DOCTOR')
    ) r(name)
    RETURNING id, name,org_id
),

-- ============================================================
-- 3. Create Privileges
-- ============================================================
privileges AS (
    INSERT INTO harmonycvi.privileges (name, org_id, is_active, created_time)
    SELECT p.name, org.id, TRUE, CURRENT_TIMESTAMP
    FROM org
    CROSS JOIN (VALUES
        ('PRIVILEGE_LIST_ORGANIZATION'),
        ('PRIVILEGE_ADD_ORGANIZATION'),
        ('PRIVILEGE_UPDATE_ORGANIZATION'),
        ('PRIVILEGE_DELETE_ORGANIZATION'),
        ('PRIVILEGE_LIST_USER'),
        ('PRIVILEGE_ADD_USER'),
        ('PRIVILEGE_UPDATE_USER'),
        ('PRIVILEGE_DELETE_USER'),
        ('PRIVILEGE_LIST_ROLE'),
        ('PRIVILEGE_LIST_STUDY'),
        ('PRIVILEGE_ADD_STUDY'),
        ('PRIVILEGE_UPDATE_STUDY'),
        ('PRIVILEGE_DELETE_STUDY'),
        ('PRIVILEGE_UPLOAD_STUDY'),
        ('PRIVILEGE_LIST_DEVICE'),
        ('PRIVILEGE_ADD_DEVICE'),
        ('PRIVILEGE_UPDATE_DEVICE'),
        ('PRIVILEGE_DELETE_DEVICE'),
        ('PRIVILEGE_LIST_CENTER'),
        ('PRIVILEGE_ADD_CENTER'),
        ('PRIVILEGE_UPDATE_CENTER'),
        ('PRIVILEGE_DELETE_CENTER'),
        ('PRIVILEGE_LIST_DOCTOR'),
        ('PRIVILEGE_ADD_DOCTOR'),
        ('PRIVILEGE_UPDATE_DOCTOR'),
        ('PRIVILEGE_DELETE_DOCTOR'),
        ('PRIVILEGE_LIST_LICENSE')
    ) p(name)
    RETURNING id, name, org_id
),

-- ============================================================
-- 4. Role–Privilege Mapping
-- ============================================================
role_priv_map AS (
    INSERT INTO harmonycvi.role_privileges (role_id, privilege_id)
    SELECT r.id, p.id
    FROM roles r
    JOIN privileges p
      ON p.org_id = r.org_id
    WHERE
    (
        -- SUPER_ADMIN
        r.name = 'SUPER_ADMIN'
        AND p.name IN (
            'PRIVILEGE_LIST_ORGANIZATION',
            'PRIVILEGE_ADD_ORGANIZATION',
            'PRIVILEGE_UPDATE_ORGANIZATION',
            'PRIVILEGE_DELETE_ORGANIZATION',
            'PRIVILEGE_LIST_USER',
            'PRIVILEGE_ADD_USER',
            'PRIVILEGE_UPDATE_USER',
            'PRIVILEGE_DELETE_USER',
            'PRIVILEGE_LIST_ROLE',
            'PRIVILEGE_LIST_STUDY',
            'PRIVILEGE_ADD_STUDY',
            'PRIVILEGE_UPDATE_STUDY',
            'PRIVILEGE_DELETE_STUDY',
            'PRIVILEGE_UPLOAD_STUDY',
            'PRIVILEGE_DELETE_DOCTOR'
        )
    )
    OR
    (
        -- ADMIN + TECHNICIAN
        r.name IN ('ADMIN', 'TECHNICIAN')
        AND p.name IN (
            'PRIVILEGE_LIST_USER',
            'PRIVILEGE_ADD_USER',
            'PRIVILEGE_UPDATE_USER',
            'PRIVILEGE_DELETE_USER',
            'PRIVILEGE_LIST_ROLE',
            'PRIVILEGE_LIST_STUDY',
            'PRIVILEGE_ADD_STUDY',
            'PRIVILEGE_UPDATE_STUDY',
            'PRIVILEGE_DELETE_STUDY',
            'PRIVILEGE_UPLOAD_STUDY',
            'PRIVILEGE_LIST_DEVICE',
            'PRIVILEGE_ADD_DEVICE',
            'PRIVILEGE_UPDATE_DEVICE',
            'PRIVILEGE_DELETE_DEVICE',
            'PRIVILEGE_LIST_CENTER',
            'PRIVILEGE_ADD_CENTER',
            'PRIVILEGE_UPDATE_CENTER',
            'PRIVILEGE_DELETE_CENTER',
            'PRIVILEGE_LIST_DOCTOR',
            'PRIVILEGE_ADD_DOCTOR',
            'PRIVILEGE_UPDATE_DOCTOR',
            'PRIVILEGE_DELETE_DOCTOR'
        )
    )
    OR
    (
        -- RESIDENT_DOCTOR + CONSULTANT_DOCTOR
        r.name IN ('RESIDENT_DOCTOR', 'CONSULTANT_DOCTOR')
        AND p.name = 'PRIVILEGE_LIST_STUDY'
    )
    RETURNING role_id
),


-- ============================================================
-- 5. Create Admin User
-- ============================================================
admin_user AS (
    INSERT INTO harmonycvi.user_details (
        email,
        password,
        role_id,
        is_active,
        created_time,
        last_updated_time,
        first_name,
        last_name
    )
    SELECT
        'admin@techvedika.com',
        '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',
        r.id,
        TRUE,
        NOW(),
        NOW(),
        'admin',
        'admin'
    FROM roles r
    WHERE r.name = 'SUPER_ADMIN'
    RETURNING id
)

-- ============================================================
-- 6. User–Organization Mapping
-- ============================================================
INSERT INTO harmonycvi.user_organizations (user_id, org_id)
SELECT u.id, o.id
FROM admin_user u
JOIN org o ON TRUE;

INSERT INTO harmonycvi.version_info (version, type) VALUES('1.21.2', 'admin'),('1.28.4', 'doctor');

INSERT INTO harmonycvi.parameter_reference (id, max, min, parameter, sex, type) VALUES
(6, '8.3', '3.9', 'co', 'm', 'lv'),
(8, '4.3', '2.1', 'ci', 'm', 'lv'),
(15, '107', '47', 'endDV_BSA', 'm', 'lv'),
(16, '47', '11', 'endSAV_BSA', 'm', 'lv'),
(5, '100', '60', 'er', 'm', 'lv'),
(43, 'N/A', 'N/A', 'ES_wall_BSA', 'f', 'lv'),
(48, '30', '14', 'endSAV_BSA', 'f', 'lv'),
(12, '75', '36', 'ED_mass_BSA', 'm', 'lv'),
(44, '59', '30', 'ED_mass_BSA', 'f', 'lv'),
(28, '75', '36', 'ED_mass_BSA', 'm', 'rv'),
(60, '59', '30', 'ED_mass_BSA', 'f', 'rv'),
(70, '75', '36', 'ED_mass_BSA', 'm', 'lv'),
(71, '75', '36', 'ED_mass_BSA', 'm', 'rv'),
(11, 'N/A', 'N/A', 'ES_wall_BSA', 'm', 'lv'),
(74, '59', '30', 'ED_mass_BSA', 'f', 'lv'),
(75, '59', '30', 'ED_mass_BSA', 'f', 'rv'),
(27, 'N/A', 'N/A', 'ES_wall_BSA', 'm', 'rv'),
(1, '207', '83', 'endDV', 'm', 'lv'),
(2, '88', '19', 'endSV', 'm', 'lv'),
(3, '127', '55', 'sv', 'm', 'lv'),
(4, '76', '51', 'ef', 'm', 'lv'),
(59, 'N/A', 'N/A', 'ES_wall_BSA', 'f', 'rv'),
(33, '155', '70', 'endDV', 'f', 'lv'),
(34, '64', '15', 'endSV', 'f', 'lv'),
(35, '99', '47', 'sv', 'f', 'lv'),
(36, '79', '52', 'ef', 'f', 'lv'),
(38, '6.9', '3.0', 'co', 'f', 'lv'),
(40, '4.0', '1.9', 'ci', 'f', 'lv'),
(47, '93', '45', 'endDV_BSA', 'f', 'lv'),
(37, '100', '60', 'er', 'f', 'lv'),
(7, '1.9', '1.7', 'bSA', 'm', 'lv'),
(39, '1.9', '1.6', 'bSA', 'f', 'lv'),
(17, '244', '87', 'endDV', 'm', 'rv'),
(18, '117', '29', 'endSV', 'm', 'rv'),
(19, '146', '43', 'sv', 'm', 'rv'),
(20, '72', '42', 'ef', 'm', 'rv'),
(22, '8.3', '2.8', 'co', 'm', 'rv'),
(24, '4.5', '1.5', 'ci', 'm', 'rv'),
(31, '123', '53', 'endDV_BSA', 'm', 'rv'),
(23, '1.9', '1.7', 'bSA', 'm', 'rv'),
(32, '59', '17', 'endSAV_BSA', 'm', 'rv'),
(21, '100', '60', 'er', 'm', 'rv'),
(49, '176', '68', 'endDV', 'f', 'rv'),
(50, '80', '20', 'endSV', 'f', 'rv'),
(51, '109', '39', 'sv', 'f', 'rv'),
(52, '74', '46', 'ef', 'f', 'rv'),
(54, '6.4', '2.4', 'co', 'f', 'rv'),
(56, '4.0', '1.6', 'ci', 'f', 'rv'),
(63, '104', '48', 'endDV_BSA', 'f', 'rv'),
(64, '40', '13', 'endSAV_BSA', 'f', 'rv'),
(53, '100', '60', 'er', 'f', 'rv'),
(55, '1.9', '1.6', 'bSA', 'f', 'rv'),
(9, '7.5', '5.5', 'ED_wall', 'm', 'lv'),
(25, '7.5', '5.5', 'ED_wall', 'm', 'rv'),
(41, '7.0', '4.0', 'ED_wall', 'f', 'lv'),
(57, '7.0', '4.0', 'ED_wall', 'f', 'rv'),
(10, '7.0', '4.1', 'ES_wall', 'm', 'lv'),
(26, '7.0', '4.1', 'ES_wall', 'm', 'rv'),
(42, '6.0', '3.5', 'ES_wall', 'f', 'lv'),
(58, '6.0', '3.5', 'ES_wall', 'f', 'rv'),
(65, '54', '17', 'ED_mass', 'm', 'rv'),
(66, '152', '57', 'ED_mass', 'm', 'lv'),
(67, '48', '13', 'ED_mass', 'f', 'rv'),
(68, '103', '43', 'ED_mass', 'f', 'lv'),
(46, 'N/A', 'N/A', 'ES_frame', 'f', 'lv'),
(14, 'N/A', 'N/A', 'ES_frame', 'm', 'lv'),
(30, 'N/A', 'N/A', 'ES_frame', 'm', 'rv'),
(62, 'N/A', 'N/A', 'ES_frame', 'f', 'rv'),
(45, 'N/A', 'N/A', 'ED_frame', 'f', 'lv'),
(13, 'N/A', 'N/A', 'ED_frame', 'm', 'lv'),
(29, 'N/A', 'N/A', 'ED_frame', 'm', 'rv'),
(61, 'N/A', 'N/A', 'ED_frame', 'f', 'rv'),
(69, 'N/A', 'N/A', 'ES_wall_BSA', 'm', 'lv'),
(72, 'N/A', 'N/A', 'ES_wall_BSA', 'm', 'rv'),
(73, 'N/A', 'N/A', 'ES_wall_BSA', 'f', 'lv'),
(76, 'N/A', 'N/A', 'ES_wall_BSA', 'f', 'rv'),
(77, '66', '30', 'si', 'm', 'lv'),
(78, '75', '28', 'si', 'm', 'rv'),
(79, '59', '30', 'si', 'f', 'lv'),
(80, '66', '29', 'si', 'f', 'rv');

SET XACT_ABORT ON;
SET QUOTED_IDENTIFIER ON;
GO

BEGIN TRANSACTION;

IF COL_LENGTH('dbo.users', 'can_register_admin') IS NULL
    ALTER TABLE dbo.users ADD can_register_admin BIT NOT NULL CONSTRAINT df_users_can_register_admin DEFAULT 0;

IF COL_LENGTH('dbo.users', 'credit_score') IS NULL
    ALTER TABLE dbo.users ADD credit_score INT NOT NULL CONSTRAINT df_users_credit_score DEFAULT 100;

UPDATE dbo.users
SET credit_score = 100
WHERE credit_score IS NULL;

DECLARE @dropUsernameConstraintSql NVARCHAR(MAX) = N'';
SELECT @dropUsernameConstraintSql = @dropUsernameConstraintSql +
    N'ALTER TABLE dbo.users DROP CONSTRAINT ' + QUOTENAME(kc.name) + N';'
FROM sys.key_constraints kc
JOIN sys.index_columns ic ON ic.object_id = kc.parent_object_id AND ic.index_id = kc.unique_index_id
JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
WHERE kc.parent_object_id = OBJECT_ID('dbo.users')
  AND kc.type = 'UQ'
  AND c.name = 'username';

IF @dropUsernameConstraintSql <> N''
    EXEC sp_executesql @dropUsernameConstraintSql;

IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uk_users_username' AND object_id = OBJECT_ID('dbo.users'))
    DROP INDEX uk_users_username ON dbo.users;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uk_users_role_username' AND object_id = OBJECT_ID('dbo.users'))
    CREATE UNIQUE INDEX uk_users_role_username ON dbo.users(role, username);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uk_users_student_no' AND object_id = OBJECT_ID('dbo.users'))
    CREATE UNIQUE INDEX uk_users_student_no ON dbo.users(student_no) WHERE student_no IS NOT NULL;

EXEC sp_executesql N'
UPDATE dbo.users
SET can_register_admin = 1,
    password = ''$2a$10$3IYgE2CZbF3gw1kGkkde7eTX434DJc59kAyqxM1uP4X0.V6sjRc0W'',
    update_time = SYSDATETIME()
WHERE username = ''admin'' AND role = ''ADMIN'';';

UPDATE dbo.users
SET password = '$2a$10$3IYgE2CZbF3gw1kGkkde7eTX434DJc59kAyqxM1uP4X0.V6sjRc0W',
    update_time = SYSDATETIME()
WHERE username = 'student' AND role = 'STUDENT';

UPDATE dbo.users
SET student_no = N'2026000001',
    phone = COALESCE(phone, N'13800000000'),
    update_time = SYSDATETIME()
WHERE username = 'student'
  AND role = 'STUDENT'
  AND (student_no IS NULL OR LEN(student_no) <> 10);

UPDATE dbo.users
SET phone = N'13800000000',
    update_time = SYSDATETIME()
WHERE username = 'student'
  AND role = 'STUDENT'
  AND (phone IS NULL OR phone = N'');

IF COL_LENGTH('dbo.study_room', 'space_type') IS NULL
    ALTER TABLE dbo.study_room ADD space_type NVARCHAR(30) NOT NULL CONSTRAINT df_study_room_space_type DEFAULT 'STUDY_ROOM';

IF COL_LENGTH('dbo.study_room', 'need_approval') IS NULL
    ALTER TABLE dbo.study_room ADD need_approval BIT NOT NULL CONSTRAINT df_study_room_need_approval DEFAULT 0;

IF COL_LENGTH('dbo.study_room', 'need_location_check') IS NULL
    ALTER TABLE dbo.study_room ADD need_location_check BIT NOT NULL CONSTRAINT df_study_room_need_location_check DEFAULT 1;

IF COL_LENGTH('dbo.study_room', 'building') IS NULL
    ALTER TABLE dbo.study_room ADD building NVARCHAR(100) NULL;

IF COL_LENGTH('dbo.study_room', 'floor_no') IS NULL
    ALTER TABLE dbo.study_room ADD floor_no NVARCHAR(50) NULL;

IF COL_LENGTH('dbo.study_room', 'manager_name') IS NULL
    ALTER TABLE dbo.study_room ADD manager_name NVARCHAR(50) NULL;

IF COL_LENGTH('dbo.study_room', 'manager_phone') IS NULL
    ALTER TABLE dbo.study_room ADD manager_phone NVARCHAR(30) NULL;

IF COL_LENGTH('dbo.study_room', 'usage_notice') IS NULL
    ALTER TABLE dbo.study_room ADD usage_notice NVARCHAR(1000) NULL;

UPDATE dbo.study_room SET space_type = 'STUDY_ROOM' WHERE space_type IS NULL OR space_type = '';
UPDATE dbo.study_room SET need_approval = 0 WHERE space_type = 'STUDY_ROOM';
UPDATE dbo.study_room SET need_approval = 1 WHERE space_type IN ('SEMINAR_ROOM', 'CLASSROOM', 'LAB_SEAT');

IF COL_LENGTH('dbo.reservation', 'purpose') IS NULL
    ALTER TABLE dbo.reservation ADD purpose NVARCHAR(500) NULL;

IF COL_LENGTH('dbo.reservation', 'participant_count') IS NULL
    ALTER TABLE dbo.reservation ADD participant_count INT NULL;

IF COL_LENGTH('dbo.reservation', 'contact_phone') IS NULL
    ALTER TABLE dbo.reservation ADD contact_phone NVARCHAR(30) NULL;

IF EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.reservation')
      AND name = 'seat_id'
      AND is_nullable = 0
)
    ALTER TABLE dbo.reservation ALTER COLUMN seat_id BIGINT NULL;

IF COL_LENGTH('dbo.reservation', 'approve_admin_id') IS NULL
    ALTER TABLE dbo.reservation ADD approve_admin_id BIGINT NULL;

IF COL_LENGTH('dbo.reservation', 'approve_time') IS NULL
    ALTER TABLE dbo.reservation ADD approve_time DATETIME2 NULL;

IF COL_LENGTH('dbo.reservation', 'reject_reason') IS NULL
    ALTER TABLE dbo.reservation ADD reject_reason NVARCHAR(500) NULL;

IF COL_LENGTH('dbo.violation_record', 'credit_deduct_points') IS NULL
    ALTER TABLE dbo.violation_record ADD credit_deduct_points INT NOT NULL CONSTRAINT df_violation_record_credit_deduct DEFAULT 0;

IF OBJECT_ID('dbo.violation_appeal', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.violation_appeal (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        violation_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        reason NVARCHAR(500) NOT NULL,
        evidence NVARCHAR(1000) NULL,
        status NVARCHAR(30) NOT NULL,
        review_admin_id BIGINT NULL,
        review_reply NVARCHAR(500) NULL,
        review_time DATETIME2 NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        update_time DATETIME2 NULL,
        CONSTRAINT fk_violation_appeal_violation FOREIGN KEY (violation_id) REFERENCES dbo.violation_record(id),
        CONSTRAINT fk_violation_appeal_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
        CONSTRAINT fk_violation_appeal_admin FOREIGN KEY (review_admin_id) REFERENCES dbo.users(id)
    );
END;

IF OBJECT_ID('dbo.repair_record', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.repair_record (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        room_id BIGINT NOT NULL,
        seat_id BIGINT NULL,
        repair_level NVARCHAR(20) NOT NULL,
        repair_type NVARCHAR(50) NOT NULL,
        description NVARCHAR(1000) NOT NULL,
        status NVARCHAR(30) NOT NULL,
        admin_id BIGINT NULL,
        admin_reply NVARCHAR(1000) NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        accept_time DATETIME2 NULL,
        process_time DATETIME2 NULL,
        finish_time DATETIME2 NULL,
        cancel_time DATETIME2 NULL,
        update_time DATETIME2 NULL,
        CONSTRAINT fk_repair_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
        CONSTRAINT fk_repair_room FOREIGN KEY (room_id) REFERENCES dbo.study_room(id),
        CONSTRAINT fk_repair_seat FOREIGN KEY (seat_id) REFERENCES dbo.seat(id),
        CONSTRAINT fk_repair_admin FOREIGN KEY (admin_id) REFERENCES dbo.users(id)
    );
END;

IF OBJECT_ID('dbo.announcement', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.announcement (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        title NVARCHAR(200) NOT NULL,
        content NVARCHAR(MAX) NOT NULL,
        type NVARCHAR(50) NOT NULL,
        pinned BIT NOT NULL DEFAULT 0,
        status NVARCHAR(30) NOT NULL DEFAULT 'PUBLISHED',
        create_admin_id BIGINT NOT NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        update_time DATETIME2 NULL,
        CONSTRAINT fk_announcement_admin FOREIGN KEY (create_admin_id) REFERENCES dbo.users(id)
    );
END;

IF OBJECT_ID('dbo.notification', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.notification (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        title NVARCHAR(200) NOT NULL,
        content NVARCHAR(1000) NOT NULL,
        type NVARCHAR(50) NOT NULL,
        read_status BIT NOT NULL DEFAULT 0,
        related_type NVARCHAR(50) NULL,
        related_id BIGINT NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        read_time DATETIME2 NULL,
        CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
    );
END;

IF OBJECT_ID('dbo.system_config', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.system_config (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        config_key NVARCHAR(100) NOT NULL UNIQUE,
        config_value NVARCHAR(500) NOT NULL,
        value_type NVARCHAR(30) NOT NULL,
        description NVARCHAR(500) NULL,
        update_admin_id BIGINT NULL,
        update_time DATETIME2 NULL,
        CONSTRAINT fk_system_config_admin FOREIGN KEY (update_admin_id) REFERENCES dbo.users(id)
    );
END;

IF OBJECT_ID('dbo.config_change_log', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.config_change_log (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        config_key NVARCHAR(100) NOT NULL,
        old_value NVARCHAR(500) NULL,
        new_value NVARCHAR(500) NOT NULL,
        admin_id BIGINT NOT NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT fk_config_change_admin FOREIGN KEY (admin_id) REFERENCES dbo.users(id)
    );
END;

IF OBJECT_ID('dbo.leave_record', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.leave_record (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        reservation_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        leave_time DATETIME2 NOT NULL,
        return_deadline DATETIME2 NOT NULL,
        return_time DATETIME2 NULL,
        status NVARCHAR(30) NOT NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT fk_leave_record_reservation FOREIGN KEY (reservation_id) REFERENCES dbo.reservation(id),
        CONSTRAINT fk_leave_record_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
    );
END;

IF OBJECT_ID('dbo.operation_log', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.operation_log (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        admin_id BIGINT NOT NULL,
        operation_type NVARCHAR(100) NOT NULL,
        operation_content NVARCHAR(1000) NOT NULL,
        target_type NVARCHAR(100) NULL,
        target_id BIGINT NULL,
        create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT fk_operation_log_admin FOREIGN KEY (admin_id) REFERENCES dbo.users(id)
    );
END;

MERGE dbo.system_config AS target
USING (VALUES
    ('first_sign_deadline_minutes', '15', 'NUMBER', 'First sign-in deadline in minutes'),
    ('leave_max_minutes', '60', 'NUMBER', 'Maximum leave duration in minutes'),
    ('grace_minutes', '5', 'NUMBER', 'Grace minutes'),
    ('ban_threshold', '3', 'NUMBER', 'Violation threshold before ban'),
    ('ban_days', '3', 'NUMBER', 'Ban days'),
    ('online_max_duration_enabled', 'true', 'BOOLEAN', 'Enable online max reservation duration'),
    ('online_max_duration_minutes', '240', 'NUMBER', 'Online max reservation duration in minutes'),
    ('location_check_enabled', 'true', 'BOOLEAN', 'Enable location check'),
    ('default_location_radius_meter', '50', 'NUMBER', 'Default location radius in meters'),
    ('ai_task_enabled', 'true', 'BOOLEAN', 'Enable AI reservation task'),
    ('approval_enabled', 'true', 'BOOLEAN', 'Enable approval flow'),
    ('repair_enabled', 'true', 'BOOLEAN', 'Enable repair flow'),
    ('credit_initial_score', '100', 'NUMBER', 'Initial student credit score'),
    ('credit_max_score', '100', 'NUMBER', 'Maximum student credit score'),
    ('credit_min_score', '0', 'NUMBER', 'Minimum student credit score'),
    ('credit_low_threshold', '60', 'NUMBER', 'Reservation block threshold for low credit'),
    ('credit_first_sign_timeout_deduct', '10', 'NUMBER', 'Credit points deducted for missing first sign-in'),
    ('credit_leave_return_timeout_deduct', '15', 'NUMBER', 'Credit points deducted for late return from leave'),
    ('low_credit_reservation_block_enabled', 'true', 'BOOLEAN', 'Block reservation when credit is lower than threshold'),
    ('violation_appeal_enabled', 'true', 'BOOLEAN', 'Enable violation appeal workflow')
) AS source (config_key, config_value, value_type, description)
ON target.config_key = source.config_key
WHEN NOT MATCHED THEN
    INSERT (config_key, config_value, value_type, description)
    VALUES (source.config_key, source.config_value, source.value_type, source.description);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_repair_record_user_status' AND object_id = OBJECT_ID('dbo.repair_record'))
    CREATE INDEX idx_repair_record_user_status ON dbo.repair_record(user_id, status, create_time);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_repair_record_room_status' AND object_id = OBJECT_ID('dbo.repair_record'))
    CREATE INDEX idx_repair_record_room_status ON dbo.repair_record(room_id, status, create_time);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_notification_user_read' AND object_id = OBJECT_ID('dbo.notification'))
    CREATE INDEX idx_notification_user_read ON dbo.notification(user_id, read_status, create_time);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_leave_record_reservation' AND object_id = OBJECT_ID('dbo.leave_record'))
    CREATE INDEX idx_leave_record_reservation ON dbo.leave_record(reservation_id, status);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_operation_log_time' AND object_id = OBJECT_ID('dbo.operation_log'))
    CREATE INDEX idx_operation_log_time ON dbo.operation_log(admin_id, operation_type, create_time);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_violation_appeal_status' AND object_id = OBJECT_ID('dbo.violation_appeal'))
    CREATE INDEX idx_violation_appeal_status ON dbo.violation_appeal(status, create_time);

COMMIT TRANSACTION;
GO

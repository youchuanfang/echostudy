SET XACT_ABORT ON;
GO

BEGIN TRANSACTION;

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
    ('repair_enabled', 'true', 'BOOLEAN', 'Enable repair flow')
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

COMMIT TRANSACTION;
GO

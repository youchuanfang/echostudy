IF OBJECT_ID('dbo.ai_reservation_task', 'U') IS NOT NULL DROP TABLE dbo.ai_reservation_task;
IF OBJECT_ID('dbo.violation_record', 'U') IS NOT NULL DROP TABLE dbo.violation_record;
IF OBJECT_ID('dbo.reservation', 'U') IS NOT NULL DROP TABLE dbo.reservation;
IF OBJECT_ID('dbo.time_node', 'U') IS NOT NULL DROP TABLE dbo.time_node;
IF OBJECT_ID('dbo.seat', 'U') IS NOT NULL DROP TABLE dbo.seat;
IF OBJECT_ID('dbo.study_room', 'U') IS NOT NULL DROP TABLE dbo.study_room;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
GO

CREATE TABLE dbo.users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL,
    password NVARCHAR(255) NOT NULL,
    real_name NVARCHAR(50) NULL,
    student_no NVARCHAR(50) NULL,
    phone NVARCHAR(30) NULL,
    role NVARCHAR(20) NOT NULL,
    status NVARCHAR(20) NOT NULL,
    violation_count INT NOT NULL DEFAULT 0,
    ban_end_time DATETIME2 NULL,
    can_register_admin BIT NOT NULL DEFAULT 0,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    update_time DATETIME2 NULL
);

CREATE UNIQUE INDEX uk_users_role_username ON dbo.users(role, username);
CREATE UNIQUE INDEX uk_users_student_no ON dbo.users(student_no) WHERE student_no IS NOT NULL;

CREATE TABLE dbo.study_room (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    location_desc NVARCHAR(255) NULL,
    capacity INT NOT NULL DEFAULT 0,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    open_status BIT NOT NULL DEFAULT 1,
    latitude DECIMAL(10,7) NULL,
    longitude DECIMAL(10,7) NULL,
    allowed_radius_meter INT NOT NULL DEFAULT 50,
    description NVARCHAR(500) NULL,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    update_time DATETIME2 NULL
);

CREATE TABLE dbo.seat (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    room_id BIGINT NOT NULL,
    seat_no NVARCHAR(30) NOT NULL,
    row_no INT NOT NULL,
    col_no INT NOT NULL,
    has_socket BIT NOT NULL DEFAULT 0,
    near_window BIT NOT NULL DEFAULT 0,
    enabled BIT NOT NULL DEFAULT 1,
    faulty BIT NOT NULL DEFAULT 0,
    remark NVARCHAR(255) NULL,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    update_time DATETIME2 NULL,
    CONSTRAINT fk_seat_room FOREIGN KEY (room_id) REFERENCES dbo.study_room(id),
    CONSTRAINT uk_seat_room_no UNIQUE (room_id, seat_no),
    CONSTRAINT uk_seat_room_grid UNIQUE (room_id, row_no, col_no)
);

CREATE TABLE dbo.time_node (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    time_value TIME NOT NULL UNIQUE,
    enabled BIT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    update_time DATETIME2 NULL
);

CREATE TABLE dbo.reservation (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    operator_admin_id BIGINT NULL,
    room_id BIGINT NOT NULL,
    seat_id BIGINT NULL,
    reserve_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status NVARCHAR(30) NOT NULL,
    source NVARCHAR(30) NOT NULL,
    sign_in_time DATETIME2 NULL,
    sign_in_latitude DECIMAL(10,7) NULL,
    sign_in_longitude DECIMAL(10,7) NULL,
    leave_time DATETIME2 NULL,
    return_deadline DATETIME2 NULL,
    return_time DATETIME2 NULL,
    finish_time DATETIME2 NULL,
    cancel_time DATETIME2 NULL,
    release_time DATETIME2 NULL,
    remark NVARCHAR(500) NULL,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    update_time DATETIME2 NULL,
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
    CONSTRAINT fk_reservation_admin FOREIGN KEY (operator_admin_id) REFERENCES dbo.users(id),
    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES dbo.study_room(id),
    CONSTRAINT fk_reservation_seat FOREIGN KEY (seat_id) REFERENCES dbo.seat(id)
);

CREATE TABLE dbo.violation_record (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reservation_id BIGINT NULL,
    violation_type NVARCHAR(50) NOT NULL,
    reason NVARCHAR(500) NOT NULL,
    violation_count_snapshot INT NOT NULL,
    ban_end_time_snapshot DATETIME2 NULL,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_violation_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
    CONSTRAINT fk_violation_reservation FOREIGN KEY (reservation_id) REFERENCES dbo.reservation(id)
);

CREATE TABLE dbo.ai_reservation_task (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    preferred_room_id BIGINT NULL,
    prefer_socket BIT NOT NULL DEFAULT 0,
    prefer_window BIT NOT NULL DEFAULT 0,
    allow_change_room BIT NOT NULL DEFAULT 1,
    status NVARCHAR(30) NOT NULL,
    execute_time DATETIME2 NULL,
    result_reservation_id BIGINT NULL,
    fail_reason NVARCHAR(500) NULL,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    update_time DATETIME2 NULL,
    CONSTRAINT fk_ai_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
    CONSTRAINT fk_ai_room FOREIGN KEY (preferred_room_id) REFERENCES dbo.study_room(id),
    CONSTRAINT fk_ai_reservation FOREIGN KEY (result_reservation_id) REFERENCES dbo.reservation(id)
);

CREATE INDEX idx_reservation_seat_time ON dbo.reservation(seat_id, reserve_date, start_time, end_time, status);
CREATE INDEX idx_reservation_user_time ON dbo.reservation(user_id, reserve_date, start_time, end_time, status);
GO

INSERT INTO dbo.users (username, password, real_name, student_no, phone, role, status, can_register_admin)
VALUES
('admin', '$2a$10$3IYgE2CZbF3gw1kGkkde7eTX434DJc59kAyqxM1uP4X0.V6sjRc0W', N'系统管理员', NULL, NULL, 'ADMIN', 'NORMAL', 1),
('student', '$2a$10$3IYgE2CZbF3gw1kGkkde7eTX434DJc59kAyqxM1uP4X0.V6sjRc0W', N'演示学生', N'2026000001', N'13800000000', 'STUDENT', 'NORMAL', 0);

INSERT INTO dbo.study_room (name, location_desc, capacity, open_time, close_time, latitude, longitude, allowed_radius_meter, description)
VALUES
(N'明德楼一楼自习室', N'明德楼 101', 24, '08:00', '22:00', 31.2304160, 121.4737010, 80, N'安静区，适合长期学习'),
(N'图书馆二楼阅览区', N'图书馆 2F 东侧', 24, '08:00', '22:00', 31.2305160, 121.4738010, 80, N'靠窗座位较多');

INSERT INTO dbo.time_node (time_value, sort_order)
VALUES
('08:00', 1), ('08:30', 2), ('09:00', 3), ('09:30', 4), ('10:00', 5), ('10:30', 6),
('11:00', 7), ('11:30', 8), ('12:00', 9), ('14:00', 10), ('14:30', 11), ('15:00', 12),
('15:30', 13), ('16:00', 14), ('16:30', 15), ('17:00', 16), ('18:00', 17), ('18:30', 18),
('19:00', 19), ('19:30', 20), ('20:00', 21), ('21:00', 22), ('22:00', 23);

DECLARE @roomId BIGINT;
DECLARE room_cursor CURSOR FOR SELECT id FROM dbo.study_room;
OPEN room_cursor;
FETCH NEXT FROM room_cursor INTO @roomId;
WHILE @@FETCH_STATUS = 0
BEGIN
    DECLARE @r INT = 1;
    WHILE @r <= 4
    BEGIN
        DECLARE @c INT = 1;
        WHILE @c <= 6
        BEGIN
            INSERT INTO dbo.seat (room_id, seat_no, row_no, col_no, has_socket, near_window)
            VALUES (@roomId, CONCAT(CHAR(64 + @r), RIGHT(CONCAT('0', @c), 2)), @r, @c,
                    CASE WHEN @c IN (1, 6) THEN 1 ELSE 0 END,
                    CASE WHEN @r = 1 THEN 1 ELSE 0 END);
            SET @c = @c + 1;
        END
        SET @r = @r + 1;
    END
    FETCH NEXT FROM room_cursor INTO @roomId;
END
CLOSE room_cursor;
DEALLOCATE room_cursor;

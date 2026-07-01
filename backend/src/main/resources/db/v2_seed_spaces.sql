SET NOCOUNT ON;

UPDATE dbo.study_room
SET open_status = 0,
    update_time = SYSDATETIME()
WHERE name LIKE N'%图书馆%'
   OR ISNULL(location_desc, N'') LIKE N'%图书馆%'
   OR ISNULL(building, N'') LIKE N'%图书馆%'
   OR name LIKE N'%明德楼%'
   OR ISNULL(location_desc, N'') LIKE N'%明德楼%'
   OR ISNULL(building, N'') LIKE N'%明德楼%';

DECLARE @spaces TABLE (
    name NVARCHAR(100) NOT NULL,
    building NVARCHAR(100) NOT NULL,
    floor_no NVARCHAR(50) NOT NULL,
    location_desc NVARCHAR(255) NOT NULL,
    space_type NVARCHAR(30) NOT NULL,
    need_approval BIT NOT NULL,
    seat_count INT NOT NULL,
    seat_prefix NVARCHAR(2) NOT NULL,
    grid_cols INT NOT NULL,
    usage_notice NVARCHAR(1000) NULL
);

DECLARE @floor INT;
DECLARE @room INT;
DECLARE @roomNo NVARCHAR(10);
DECLARE @name NVARCHAR(100);

SET @floor = 2;
WHILE @floor <= 9
BEGIN
    SET @room = 1;
    WHILE @room <= 4
    BEGIN
        SET @roomNo = CONCAT(CAST(@floor AS NVARCHAR(2)), RIGHT(CONCAT(N'0', CAST(@room AS NVARCHAR(2))), 2));
        SET @name = CONCAT(N'综合楼', @roomNo);

        INSERT INTO @spaces (name, building, floor_no, location_desc, space_type, need_approval, seat_count, seat_prefix, grid_cols, usage_notice)
        VALUES (@name, N'综合楼', CAST(@floor AS NVARCHAR(10)), @name, N'CLASSROOM', 1, 40, N'A', 8, N'空闲教室预约后请按时使用，保持教室整洁。');

        SET @room = @room + 1;
    END
    SET @floor = @floor + 1;
END

SET @floor = 2;
WHILE @floor <= 10
BEGIN
    SET @room = 1;
    WHILE @room <= 4
    BEGIN
        SET @roomNo = CONCAT(CAST(@floor AS NVARCHAR(2)), RIGHT(CONCAT(N'0', CAST(@room AS NVARCHAR(2))), 2));
        SET @name = CONCAT(N'科研楼', @roomNo);

        INSERT INTO @spaces (name, building, floor_no, location_desc, space_type, need_approval, seat_count, seat_prefix, grid_cols, usage_notice)
        VALUES (@name, N'科研楼', CAST(@floor AS NVARCHAR(10)), @name, N'CLASSROOM', 1, 40, N'A', 8, N'空闲教室预约后请按时使用，保持教室整洁。');

        SET @room = @room + 1;
    END
    SET @floor = @floor + 1;
END

INSERT INTO @spaces (name, building, floor_no, location_desc, space_type, need_approval, seat_count, seat_prefix, grid_cols, usage_notice)
VALUES
    (N'科研楼1010', N'科研楼', N'10', N'科研楼1010', N'LAB_SEAT', 1, 30, N'W', 10, N'实验室空间需审批后使用，请遵守实验室安全规范。'),
    (N'科研楼1115', N'科研楼', N'11', N'科研楼1115', N'LAB_SEAT', 1, 30, N'W', 10, N'实验室空间需审批后使用，请遵守实验室安全规范。'),
    (N'科研楼1104', N'科研楼', N'11', N'科研楼1104', N'LAB_SEAT', 1, 30, N'W', 10, N'实验室空间需审批后使用，请遵守实验室安全规范。'),
    (N'科研楼B101-A', N'科研楼', N'1', N'B1 科研楼B101-A', N'LAB_SEAT', 1, 30, N'W', 10, N'地下实验室空间需审批后使用，B1 信息以位置描述为准。'),
    (N'科研楼B101-B', N'科研楼', N'1', N'B1 科研楼B101-B', N'LAB_SEAT', 1, 30, N'W', 10, N'地下实验室空间需审批后使用，B1 信息以位置描述为准。'),
    (N'科研楼B102', N'科研楼', N'1', N'B1 科研楼B102', N'LAB_SEAT', 1, 30, N'W', 10, N'地下实验室空间需审批后使用，B1 信息以位置描述为准。'),
    (N'科研楼B103', N'科研楼', N'1', N'B1 科研楼B103', N'LAB_SEAT', 1, 30, N'W', 10, N'地下实验室空间需审批后使用，B1 信息以位置描述为准。'),
    (N'科研楼1015', N'科研楼', N'10', N'科研楼1015', N'LAB_SEAT', 1, 20, N'W', 5, N'实验室工位需审批后使用，请按预约工位就座。'),
    (N'科研楼1016', N'科研楼', N'10', N'科研楼1016', N'LAB_SEAT', 1, 20, N'W', 5, N'实验室工位需审批后使用，请按预约工位就座。'),
    (N'科研楼1017', N'科研楼', N'10', N'科研楼1017', N'LAB_SEAT', 1, 20, N'W', 5, N'实验室工位需审批后使用，请按预约工位就座。'),
    (N'科研楼1110', N'科研楼', N'11', N'科研楼1110', N'LAB_SEAT', 1, 20, N'W', 5, N'实验室工位需审批后使用，请按预约工位就座。'),
    (N'科研楼1112', N'科研楼', N'11', N'科研楼1112', N'LAB_SEAT', 1, 20, N'W', 5, N'实验室工位需审批后使用，请按预约工位就座。'),
    (N'综合楼101', N'综合楼', N'1', N'综合楼101', N'SEMINAR_ROOM', 1, 12, N'A', 4, N'研讨室预约需审批，请按申请用途使用。'),
    (N'综合楼102', N'综合楼', N'1', N'综合楼102', N'SEMINAR_ROOM', 1, 12, N'A', 4, N'研讨室预约需审批，请按申请用途使用。'),
    (N'综合楼103', N'综合楼', N'1', N'综合楼103', N'SEMINAR_ROOM', 1, 12, N'A', 4, N'研讨室预约需审批，请按申请用途使用。'),
    (N'综合楼104', N'综合楼', N'1', N'综合楼104', N'SEMINAR_ROOM', 1, 12, N'A', 4, N'研讨室预约需审批，请按申请用途使用。'),
    (N'教三101', N'教学楼3', N'1', N'教学楼3 1楼 教三101', N'STUDY_ROOM', 0, 60, N'A', 10, N'自习室无需审批，请保持安静并按预约座位就座。'),
    (N'教三110', N'教学楼3', N'1', N'教学楼3 1楼 教三110', N'STUDY_ROOM', 0, 60, N'A', 10, N'自习室无需审批，请保持安静并按预约座位就座。'),
    (N'学19楼下自习室', N'学19楼', N'1', N'学19楼 B1', N'STUDY_ROOM', 0, 60, N'A', 10, N'地下自习室无需审批，B1 信息以位置描述为准。');

DELETE s
FROM dbo.seat s
JOIN dbo.study_room r ON r.id = s.room_id
JOIN @spaces p ON p.name = r.name
    AND p.building = ISNULL(r.building, N'')
    AND p.floor_no = ISNULL(r.floor_no, N'')
WHERE p.space_type IN (N'CLASSROOM', N'SEMINAR_ROOM')
  AND NOT EXISTS (SELECT 1 FROM dbo.reservation rv WHERE rv.seat_id = s.id);

DECLARE space_cursor CURSOR LOCAL FAST_FORWARD FOR
    SELECT name, building, floor_no, location_desc, space_type, need_approval, seat_count, seat_prefix, grid_cols, usage_notice
    FROM @spaces;

DECLARE @building NVARCHAR(100);
DECLARE @floorNo NVARCHAR(50);
DECLARE @locationDesc NVARCHAR(255);
DECLARE @spaceType NVARCHAR(30);
DECLARE @needApproval BIT;
DECLARE @seatCount INT;
DECLARE @seatPrefix NVARCHAR(2);
DECLARE @gridCols INT;
DECLARE @usageNotice NVARCHAR(1000);
DECLARE @roomId BIGINT;
DECLARE @seatNo NVARCHAR(30);
DECLARE @rowNo INT;
DECLARE @colNo INT;
DECLARE @i INT;

OPEN space_cursor;
FETCH NEXT FROM space_cursor INTO @name, @building, @floorNo, @locationDesc, @spaceType, @needApproval, @seatCount, @seatPrefix, @gridCols, @usageNotice;

WHILE @@FETCH_STATUS = 0
BEGIN
    SELECT @roomId = id
    FROM dbo.study_room
    WHERE name = @name
      AND ISNULL(building, N'') = @building
      AND ISNULL(floor_no, N'') = @floorNo;

    IF @roomId IS NULL
    BEGIN
        INSERT INTO dbo.study_room (
            name, space_type, building, floor_no, location_desc, capacity,
            open_time, close_time, open_status, need_approval, need_location_check,
            allowed_radius_meter, usage_notice, create_time
        )
        VALUES (
            @name, @spaceType, @building, @floorNo, @locationDesc, @seatCount,
            '08:00', '22:00', 1, @needApproval, 1,
            50, @usageNotice, SYSDATETIME()
        );

        SET @roomId = SCOPE_IDENTITY();
    END
    ELSE
    BEGIN
        UPDATE dbo.study_room
        SET space_type = @spaceType,
            building = @building,
            floor_no = @floorNo,
            location_desc = @locationDesc,
            capacity = @seatCount,
            open_time = '08:00',
            close_time = '22:00',
            open_status = 1,
            need_approval = @needApproval,
            need_location_check = 1,
            allowed_radius_meter = 50,
            usage_notice = @usageNotice,
            update_time = SYSDATETIME()
        WHERE id = @roomId;
    END

    SET @i = 1;
    WHILE @i <= @seatCount AND @spaceType IN (N'STUDY_ROOM', N'PUBLIC_AREA', N'LAB_SEAT')
    BEGIN
        SET @seatNo = CONCAT(@seatPrefix, RIGHT(CONCAT(N'00', CAST(@i AS NVARCHAR(3))), 2));
        SET @rowNo = ((@i - 1) / @gridCols) + 1;
        SET @colNo = ((@i - 1) % @gridCols) + 1;

        IF NOT EXISTS (SELECT 1 FROM dbo.seat WHERE room_id = @roomId AND seat_no = @seatNo)
           AND NOT EXISTS (SELECT 1 FROM dbo.seat WHERE room_id = @roomId AND row_no = @rowNo AND col_no = @colNo)
        BEGIN
            INSERT INTO dbo.seat (
                room_id, seat_no, row_no, col_no, has_socket, near_window,
                enabled, faulty, create_time
            )
            VALUES (
                @roomId, @seatNo, @rowNo, @colNo,
                CASE WHEN @i % 3 = 0 THEN 1 ELSE 0 END,
                CASE WHEN @colNo = 1 OR @colNo = @gridCols THEN 1 ELSE 0 END,
                1, 0, SYSDATETIME()
            );
        END

        SET @i = @i + 1;
    END

    SET @roomId = NULL;
    FETCH NEXT FROM space_cursor INTO @name, @building, @floorNo, @locationDesc, @spaceType, @needApproval, @seatCount, @seatPrefix, @gridCols, @usageNotice;
END

CLOSE space_cursor;
DEALLOCATE space_cursor;

SELECT COUNT(*) AS seeded_space_count
FROM dbo.study_room
WHERE (building = N'综合楼' AND (name LIKE N'综合楼[1-9]%' OR name IN (N'综合楼101', N'综合楼102', N'综合楼103', N'综合楼104')))
   OR (building = N'科研楼' AND name LIKE N'科研楼%')
   OR name IN (N'教三101', N'教三110', N'学19楼下自习室');

SELECT COUNT(*) AS seeded_seat_count
FROM dbo.seat s
JOIN dbo.study_room r ON r.id = s.room_id
WHERE (r.building = N'综合楼' AND (r.name LIKE N'综合楼[1-9]%' OR r.name IN (N'综合楼101', N'综合楼102', N'综合楼103', N'综合楼104')))
   OR (r.building = N'科研楼' AND r.name LIKE N'科研楼%')
   OR r.name IN (N'教三101', N'教三110', N'学19楼下自习室');

package com.echostudy.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.entity.Seat;
import com.echostudy.entity.StudyRoom;
import com.echostudy.entity.TimeNode;
import com.echostudy.entity.User;
import com.echostudy.enums.UserRole;
import com.echostudy.enums.UserStatus;
import com.echostudy.mapper.SeatMapper;
import com.echostudy.mapper.StudyRoomMapper;
import com.echostudy.mapper.TimeNodeMapper;
import com.echostudy.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final SeatMapper seatMapper;
    private final TimeNodeMapper timeNodeMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initUsers();
        initRoomsAndSeats();
        initTimeNodes();
    }

    private void initUsers() {
        createUserIfAbsent("admin", "系统管理员", null, UserRole.ADMIN.name());
        createUserIfAbsent("student", "演示学生", "20260001", UserRole.STUDENT.name());
    }

    private void createUserIfAbsent(String username, String realName, String studentNo, String role) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count > 0) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRealName(realName);
        user.setStudentNo(studentNo);
        user.setRole(role);
        user.setStatus(UserStatus.NORMAL.name());
        user.setViolationCount(0);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        log.info("EchoStudy initialized default {} account: {}", role, username);
    }

    private void initRoomsAndSeats() {
        if (studyRoomMapper.selectCount(null) > 0) {
            return;
        }
        createRoomWithSeats("明德楼一楼自习室", "明德楼 101", "安静区，适合长期学习");
        createRoomWithSeats("图书馆二楼阅览区", "图书馆 2F 东侧", "靠窗座位较多");
    }

    private void createRoomWithSeats(String name, String location, String description) {
        StudyRoom room = new StudyRoom();
        room.setName(name);
        room.setLocationDesc(location);
        room.setCapacity(24);
        room.setOpenTime(LocalTime.of(8, 0));
        room.setCloseTime(LocalTime.of(22, 0));
        room.setOpenStatus(true);
        room.setLatitude(new BigDecimal("31.2304160"));
        room.setLongitude(new BigDecimal("121.4737010"));
        room.setAllowedRadiusMeter(80);
        room.setDescription(description);
        room.setCreateTime(LocalDateTime.now());
        studyRoomMapper.insert(room);
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 6; col++) {
                Seat seat = new Seat();
                seat.setRoomId(room.getId());
                seat.setSeatNo((char) ('A' + row - 1) + String.format("%02d", col));
                seat.setRowNo(row);
                seat.setColNo(col);
                seat.setHasSocket(col == 1 || col == 6);
                seat.setNearWindow(row == 1);
                seat.setEnabled(true);
                seat.setFaulty(false);
                seat.setCreateTime(LocalDateTime.now());
                seatMapper.insert(seat);
            }
        }
    }

    private void initTimeNodes() {
        if (timeNodeMapper.selectCount(null) > 0) {
            return;
        }
        List<String> values = List.of("08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "12:00", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "18:00", "18:30",
                "19:00", "19:30", "20:00", "21:00", "22:00");
        for (int i = 0; i < values.size(); i++) {
            TimeNode node = new TimeNode();
            node.setTimeValue(LocalTime.parse(values.get(i)));
            node.setEnabled(true);
            node.setSortOrder(i + 1);
            node.setCreateTime(LocalDateTime.now());
            timeNodeMapper.insert(node);
        }
    }
}

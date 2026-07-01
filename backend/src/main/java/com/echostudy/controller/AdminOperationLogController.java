package com.echostudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.common.Result;
import com.echostudy.entity.OperationLog;
import com.echostudy.entity.User;
import com.echostudy.mapper.OperationLogMapper;
import com.echostudy.mapper.UserMapper;
import com.echostudy.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/operation-logs")
public class AdminOperationLogController {

    private final OperationLogMapper operationLogMapper;
    private final UserMapper userMapper;

    @GetMapping
    public Result<List<OperationLogVO>> list(@RequestParam(required = false) Long adminId,
                                            @RequestParam(required = false) String operationType,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                            @RequestParam(required = false) String keyword) {
        List<OperationLogVO> rows = operationLogMapper.selectList(new LambdaQueryWrapper<OperationLog>()
                        .eq(adminId != null, OperationLog::getAdminId, adminId)
                        .eq(operationType != null && !operationType.isBlank(), OperationLog::getOperationType, operationType)
                        .ge(startDate != null, OperationLog::getCreateTime, startDate == null ? null : LocalDateTime.of(startDate, LocalTime.MIN))
                        .lt(endDate != null, OperationLog::getCreateTime, endDate == null ? null : LocalDateTime.of(endDate.plusDays(1), LocalTime.MIN))
                        .orderByDesc(OperationLog::getCreateTime))
                .stream().map(this::toVO).toList();
        if (keyword == null || keyword.isBlank()) {
            return Result.ok(rows);
        }
        String term = keyword.toLowerCase();
        return Result.ok(rows.stream()
                .filter(row -> contains(row.getAdminName(), term)
                        || contains(row.getOperationType(), term)
                        || contains(row.getOperationContent(), term)
                        || contains(row.getTargetType(), term))
                .toList());
    }

    private OperationLogVO toVO(OperationLog log) {
        OperationLogVO vo = OperationLogVO.from(log);
        User admin = userMapper.selectById(log.getAdminId());
        if (admin != null) {
            vo.setAdminName(admin.getRealName() == null ? admin.getUsername() : admin.getRealName());
        }
        return vo;
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }
}

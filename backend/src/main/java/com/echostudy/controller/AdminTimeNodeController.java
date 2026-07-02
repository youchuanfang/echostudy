package com.echostudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.common.Result;
import com.echostudy.dto.TimeNodeRequest;
import com.echostudy.entity.TimeNode;
import com.echostudy.exception.BusinessException;
import com.echostudy.mapper.TimeNodeMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/time-nodes")
public class AdminTimeNodeController {

    private static final DateTimeFormatter SQL_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final TimeNodeMapper timeNodeMapper;

    @GetMapping
    @Transactional
    public Result<List<TimeNode>> list() {
        reorderAll();
        return Result.ok(timeNodeMapper.selectList(new LambdaQueryWrapper<TimeNode>()
                .orderByAsc(TimeNode::getTimeValue)));
    }

    @PostMapping
    @Transactional
    public Result<TimeNode> create(@Valid @RequestBody TimeNodeRequest request) {
        requireUniqueTime(request.getTimeValue(), null);
        TimeNode node = toEntity(request);
        node.setCreateTime(LocalDateTime.now());
        timeNodeMapper.insert(node);
        reorderAll();
        return Result.ok(timeNodeMapper.selectById(node.getId()));
    }

    @PutMapping("/{id}")
    @Transactional
    public Result<TimeNode> update(@PathVariable Long id, @Valid @RequestBody TimeNodeRequest request) {
        if (timeNodeMapper.selectById(id) == null) {
            throw new BusinessException(400, "时间节点不存在");
        }
        requireUniqueTime(request.getTimeValue(), id);
        TimeNode node = toEntity(request);
        node.setId(id);
        node.setUpdateTime(LocalDateTime.now());
        timeNodeMapper.updateById(node);
        reorderAll();
        return Result.ok(timeNodeMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Long id) {
        timeNodeMapper.deleteById(id);
        reorderAll();
        return Result.ok();
    }

    private TimeNode toEntity(TimeNodeRequest request) {
        TimeNode node = new TimeNode();
        node.setTimeValue(request.getTimeValue());
        node.setEnabled(request.getEnabled());
        node.setSortOrder(0);
        return node;
    }

    private void requireUniqueTime(LocalTime timeValue, Long excludeId) {
        LambdaQueryWrapper<TimeNode> query = new LambdaQueryWrapper<TimeNode>()
                .apply("time_value = CAST({0} AS time)", sqlTime(timeValue))
                .ne(excludeId != null, TimeNode::getId, excludeId);
        if (timeNodeMapper.selectCount(query) > 0) {
            throw new BusinessException(400, "该节点已存在");
        }
    }

    private void reorderAll() {
        List<TimeNode> nodes = timeNodeMapper.selectList(new LambdaQueryWrapper<TimeNode>()
                .orderByAsc(TimeNode::getTimeValue));
        for (int i = 0; i < nodes.size(); i++) {
            TimeNode node = nodes.get(i);
            int nextSortOrder = i + 1;
            if (node.getSortOrder() == null || node.getSortOrder() != nextSortOrder) {
                TimeNode update = new TimeNode();
                update.setId(node.getId());
                update.setSortOrder(nextSortOrder);
                update.setUpdateTime(LocalDateTime.now());
                timeNodeMapper.updateById(update);
            }
        }
    }

    private String sqlTime(LocalTime time) {
        return time.format(SQL_TIME_FORMATTER);
    }
}

package com.echostudy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.common.Result;
import com.echostudy.dto.TimeNodeRequest;
import com.echostudy.entity.TimeNode;
import com.echostudy.mapper.TimeNodeMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/time-nodes")
public class AdminTimeNodeController {

    private final TimeNodeMapper timeNodeMapper;

    @GetMapping
    public Result<List<TimeNode>> list() {
        return Result.ok(timeNodeMapper.selectList(new LambdaQueryWrapper<TimeNode>().orderByAsc(TimeNode::getSortOrder)));
    }

    @PostMapping
    public Result<TimeNode> create(@Valid @RequestBody TimeNodeRequest request) {
        TimeNode node = toEntity(request);
        node.setCreateTime(LocalDateTime.now());
        timeNodeMapper.insert(node);
        return Result.ok(node);
    }

    @PutMapping("/{id}")
    public Result<TimeNode> update(@PathVariable Long id, @Valid @RequestBody TimeNodeRequest request) {
        TimeNode node = toEntity(request);
        node.setId(id);
        node.setUpdateTime(LocalDateTime.now());
        timeNodeMapper.updateById(node);
        return Result.ok(timeNodeMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        timeNodeMapper.deleteById(id);
        return Result.ok();
    }

    private TimeNode toEntity(TimeNodeRequest request) {
        TimeNode node = new TimeNode();
        node.setTimeValue(request.getTimeValue());
        node.setEnabled(request.getEnabled());
        node.setSortOrder(request.getSortOrder());
        return node;
    }
}

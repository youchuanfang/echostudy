package com.echostudy.controller;

import com.echostudy.common.Result;
import com.echostudy.dto.ConfigUpdateRequest;
import com.echostudy.service.ConfigService;
import com.echostudy.vo.ConfigChangeLogVO;
import com.echostudy.vo.SystemConfigVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminConfigController {

    private final ConfigService configService;

    @GetMapping("/configs")
    public Result<List<SystemConfigVO>> configs() {
        return Result.ok(configService.list());
    }

    @PutMapping("/configs/{key}")
    public Result<SystemConfigVO> update(@PathVariable String key, @Valid @RequestBody ConfigUpdateRequest request) {
        return Result.ok(configService.update(key, request.getConfigValue()));
    }

    @GetMapping("/config-logs")
    public Result<List<ConfigChangeLogVO>> logs() {
        return Result.ok(configService.logs());
    }
}

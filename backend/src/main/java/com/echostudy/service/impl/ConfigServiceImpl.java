package com.echostudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echostudy.entity.ConfigChangeLog;
import com.echostudy.entity.OperationLog;
import com.echostudy.entity.SystemConfig;
import com.echostudy.entity.User;
import com.echostudy.mapper.ConfigChangeLogMapper;
import com.echostudy.mapper.OperationLogMapper;
import com.echostudy.mapper.SystemConfigMapper;
import com.echostudy.mapper.UserMapper;
import com.echostudy.security.UserContext;
import com.echostudy.service.ConfigService;
import com.echostudy.vo.ConfigChangeLogVO;
import com.echostudy.vo.SystemConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final SystemConfigMapper systemConfigMapper;
    private final ConfigChangeLogMapper configChangeLogMapper;
    private final OperationLogMapper operationLogMapper;
    private final UserMapper userMapper;

    @Override
    public List<SystemConfigVO> list() {
        return systemConfigMapper.selectList(new LambdaQueryWrapper<SystemConfig>().orderByAsc(SystemConfig::getConfigKey))
                .stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public SystemConfigVO update(String key, String value) {
        SystemConfig config = systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        String oldValue = config == null ? null : config.getConfigValue();
        if (config == null) {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setValueType(resolveValueType(value));
            config.setDescription(key);
        }
        config.setConfigValue(value);
        config.setUpdateAdminId(UserContext.getUserId());
        config.setUpdateTime(LocalDateTime.now());
        if (config.getId() == null) {
            systemConfigMapper.insert(config);
        } else {
            systemConfigMapper.updateById(config);
        }

        ConfigChangeLog changeLog = new ConfigChangeLog();
        changeLog.setConfigKey(key);
        changeLog.setOldValue(oldValue);
        changeLog.setNewValue(value);
        changeLog.setAdminId(UserContext.getUserId());
        changeLog.setCreateTime(LocalDateTime.now());
        configChangeLogMapper.insert(changeLog);

        OperationLog operationLog = new OperationLog();
        operationLog.setAdminId(UserContext.getUserId());
        operationLog.setOperationType("修改系统规则");
        operationLog.setOperationContent("修改配置 " + key + "：" + oldValue + " -> " + value);
        operationLog.setTargetType("SYSTEM_CONFIG");
        operationLog.setTargetId(config.getId());
        operationLog.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(operationLog);
        return toVO(config);
    }

    @Override
    public List<ConfigChangeLogVO> logs() {
        return configChangeLogMapper.selectList(new LambdaQueryWrapper<ConfigChangeLog>().orderByDesc(ConfigChangeLog::getCreateTime))
                .stream().map(this::toVO).toList();
    }

    @Override
    public int getInt(String key, long defaultValue) {
        String value = getValue(key);
        if (value == null) {
            return Math.toIntExact(defaultValue);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return Math.toIntExact(defaultValue);
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getValue(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    private String getValue(String key) {
        SystemConfig config = systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        return config == null ? null : config.getConfigValue();
    }

    private SystemConfigVO toVO(SystemConfig config) {
        SystemConfigVO vo = SystemConfigVO.from(config);
        User admin = config.getUpdateAdminId() == null ? null : userMapper.selectById(config.getUpdateAdminId());
        if (admin != null) {
            vo.setUpdateAdminName(admin.getRealName() == null ? admin.getUsername() : admin.getRealName());
        }
        return vo;
    }

    private ConfigChangeLogVO toVO(ConfigChangeLog log) {
        ConfigChangeLogVO vo = ConfigChangeLogVO.from(log);
        User admin = userMapper.selectById(log.getAdminId());
        if (admin != null) {
            vo.setAdminName(admin.getRealName() == null ? admin.getUsername() : admin.getRealName());
        }
        return vo;
    }

    private String resolveValueType(String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return "BOOLEAN";
        }
        try {
            Integer.parseInt(value);
            return "NUMBER";
        } catch (NumberFormatException ignored) {
            return "STRING";
        }
    }
}

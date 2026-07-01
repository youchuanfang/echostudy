package com.echostudy.vo;

import com.echostudy.entity.SystemConfig;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemConfigVO {
    private Long id;
    private String configKey;
    private String configValue;
    private String valueType;
    private String description;
    private Long updateAdminId;
    private String updateAdminName;
    private LocalDateTime updateTime;

    public static SystemConfigVO from(SystemConfig config) {
        SystemConfigVO vo = new SystemConfigVO();
        vo.setId(config.getId());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setValueType(config.getValueType());
        vo.setDescription(config.getDescription());
        vo.setUpdateAdminId(config.getUpdateAdminId());
        vo.setUpdateTime(config.getUpdateTime());
        return vo;
    }
}

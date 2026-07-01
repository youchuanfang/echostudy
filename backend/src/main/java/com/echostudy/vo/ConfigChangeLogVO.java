package com.echostudy.vo;

import com.echostudy.entity.ConfigChangeLog;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfigChangeLogVO {
    private Long id;
    private String configKey;
    private String oldValue;
    private String newValue;
    private Long adminId;
    private String adminName;
    private LocalDateTime createTime;

    public static ConfigChangeLogVO from(ConfigChangeLog log) {
        ConfigChangeLogVO vo = new ConfigChangeLogVO();
        vo.setId(log.getId());
        vo.setConfigKey(log.getConfigKey());
        vo.setOldValue(log.getOldValue());
        vo.setNewValue(log.getNewValue());
        vo.setAdminId(log.getAdminId());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }
}

package com.echostudy.service;

import com.echostudy.vo.ConfigChangeLogVO;
import com.echostudy.vo.SystemConfigVO;

import java.util.List;

public interface ConfigService {

    List<SystemConfigVO> list();

    SystemConfigVO update(String key, String value);

    List<ConfigChangeLogVO> logs();

    int getInt(String key, long defaultValue);

    boolean getBoolean(String key, boolean defaultValue);
}

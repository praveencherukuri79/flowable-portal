package com.example.backend.service;

import com.example.backend.dto.EngineInfoDto;
import java.util.Map;

public interface FlowableEngineInfoService {
    EngineInfoDto getEngineInfo();
    Map<String, Object> getEngineProperties();
    Map<String, Object> getTableCounts();
    Map<String, Object> getJobStatistics();
    Map<String, Object> getSystemHealth();
}
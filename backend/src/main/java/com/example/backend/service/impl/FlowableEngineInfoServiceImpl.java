package com.example.backend.service.impl;

import com.example.backend.dto.EngineInfoDto;
import com.example.backend.service.FlowableEngineInfoService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FlowableEngineInfoServiceImpl implements FlowableEngineInfoService {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private ManagementService managementService;

    @Override
    public EngineInfoDto getEngineInfo() {
        EngineInfoDto dto = new EngineInfoDto();
        dto.name = processEngine.getName();
        dto.version = ProcessEngine.VERSION;
        dto.resourceUrl = "http://localhost:8080/api/flowable/engine";
        return dto;
    }

    @Override
    public Map<String, Object> getEngineProperties() {
        Map<String, Object> properties = new HashMap<>();
        managementService.getProperties().forEach(properties::put);
        return properties;
    }

    @Override
    public Map<String, Object> getTableCounts() {
        Map<String, Object> tableCounts = new HashMap<>();
        managementService.getTableCount().forEach((k, v) -> tableCounts.put(k, v));
        return tableCounts;
    }

    @Override
    public Map<String, Object> getJobStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long jobs = managementService.createJobQuery().count();
            long timerJobs = managementService.createTimerJobQuery().count();
            long suspendedJobs = managementService.createSuspendedJobQuery().count();
            long deadLetterJobs = managementService.createDeadLetterJobQuery().count();
            
            stats.put("jobs", jobs);
            stats.put("timerJobs", timerJobs);
            stats.put("suspendedJobs", suspendedJobs);
            stats.put("deadLetterJobs", deadLetterJobs);
            stats.put("totalJobs", jobs + timerJobs + suspendedJobs + deadLetterJobs);
        } catch (Exception e) {
            stats.put("error", "Unable to retrieve job statistics: " + e.getMessage());
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Engine status
            health.put("engineName", processEngine.getName());
            health.put("engineVersion", ProcessEngine.VERSION);
            health.put("status", "UP");
            
            // Database connectivity
            Map<String, Object> dbHealth = new HashMap<>();
            dbHealth.put("status", "UP");
            dbHealth.put("type", managementService.getProperties().get("database.type"));
            health.put("database", dbHealth);
            
            // Table counts as health indicator
            Map<String, Long> tableCounts = managementService.getTableCount();
            Map<String, Object> tableInfo = new HashMap<>();
            tableCounts.forEach((k, v) -> tableInfo.put(k, v));
            health.put("tableInfo", tableInfo);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return health;
    }
}
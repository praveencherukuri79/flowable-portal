package com.example.backend.service.impl;

import com.example.backend.dto.MetricsDto;
import com.example.backend.service.AdminMetricsService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class AdminMetricsServiceImpl implements AdminMetricsService {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    @Override
    public MetricsDto getMetrics() {
        MetricsDto dto = new MetricsDto();

        // Calculate running and completed instances
        long runningInstances = historyService.createHistoricProcessInstanceQuery().unfinished().count();
        long completedInstances = historyService.createHistoricProcessInstanceQuery().finished().count();
        long totalTasks = taskService.createTaskQuery().count();

        dto.runningInstances = runningInstances;
        dto.completedInstances = completedInstances;
        dto.totalTasks = totalTasks;

        // Instances by day (7 days)
        List<MetricsDto.DailyCount> dailyCounts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Date start = Date.from(LocalDate.now().minusDays(i).atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(LocalDate.now().minusDays(i - 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            long count = historyService.createHistoricProcessInstanceQuery()
                    .startedAfter(start).startedBefore(end).count();
            
            MetricsDto.DailyCount dc = new MetricsDto.DailyCount();
            dc.day = LocalDate.now().minusDays(i).toString();
            dc.count = count;
            dailyCounts.add(dc);
        }

        MetricsDto.StateCount claimable = new MetricsDto.StateCount();
        claimable.state = "CLAIMABLE";
        claimable.count = taskService.createTaskQuery().taskUnassigned().count();

        MetricsDto.StateCount assigned = new MetricsDto.StateCount();
        assigned.state = "ASSIGNED";
        assigned.count = taskService.createTaskQuery().taskAssigned().count();

        dto.instancesByDay = dailyCounts;
        dto.tasksByState = List.of(claimable, assigned);
        dto.avgDurationByDefinition = Collections.emptyList();
        return dto;
    }
}


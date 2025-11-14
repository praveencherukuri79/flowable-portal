package com.example.backend.service.impl;

import com.example.backend.dto.HistoricProcessInstanceDto;
import com.example.backend.dto.HistoricTaskInstanceDto;
import com.example.backend.service.FlowableHistoryService;
import com.example.backend.util.DtoMapper;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FlowableHistoryServiceImpl implements FlowableHistoryService {

    @Autowired
    private HistoryService historyService;

    @Override
    public List<HistoricProcessInstanceDto> getProcessHistory(String processKey) {
        return historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processKey)
                .list()
                .stream()
                .map(this::toHistoricProcessInstanceDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<HistoricProcessInstanceDto> getAllProcessHistory() {
        return historyService.createHistoricProcessInstanceQuery()
                .list()
                .stream()
                .map(this::toHistoricProcessInstanceDto)
                .collect(Collectors.toList());
    }

    @Override
    public HistoricProcessInstanceDto getHistoricProcessInstance(String processInstanceId) {
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        return hpi != null ? toHistoricProcessInstanceDto(hpi) : null;
    }

    @Override
    public List<HistoricTaskInstanceDto> getTaskHistory(String processInstanceId) {
        return historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .list()
                .stream()
                .map(this::toHistoricTaskInstanceDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<HistoricTaskInstanceDto> getTaskHistoryByUser(String user) {
        return historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(user)
                .list()
                .stream()
                .map(this::toHistoricTaskInstanceDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<HistoricTaskInstanceDto> getCompletedTasks() {
        return historyService.createHistoricTaskInstanceQuery()
                .finished()
                .list()
                .stream()
                .map(this::toHistoricTaskInstanceDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getProcessStatistics() {
        long totalProcesses = historyService.createHistoricProcessInstanceQuery().count();
        long finishedProcesses = historyService.createHistoricProcessInstanceQuery().finished().count();
        long unfinishedProcesses = historyService.createHistoricProcessInstanceQuery().unfinished().count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProcesses", totalProcesses);
        stats.put("finishedProcesses", finishedProcesses);
        stats.put("unfinishedProcesses", unfinishedProcesses);
        stats.put("completionRate", totalProcesses > 0 ? (double) finishedProcesses / totalProcesses * 100 : 0);
        
        return stats;
    }

    @Override
    public Map<String, Object> getTaskStatistics() {
        long totalTasks = historyService.createHistoricTaskInstanceQuery().count();
        long completedTasks = historyService.createHistoricTaskInstanceQuery().finished().count();
        long pendingTasks = totalTasks - completedTasks;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", totalTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("pendingTasks", pendingTasks);
        stats.put("completionRate", totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);
        
        return stats;
    }

    @Override
    public List<HistoricProcessInstanceDto> getProcessHistoryByDateRange(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            
            return historyService.createHistoricProcessInstanceQuery()
                    .startedAfter(start)
                    .startedBefore(end)
                    .list()
                    .stream()
                    .map(this::toHistoricProcessInstanceDto)
                    .collect(Collectors.toList());
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format. Use yyyy-MM-dd", e);
        }
    }

    private HistoricProcessInstanceDto toHistoricProcessInstanceDto(HistoricProcessInstance hpi) {
        return DtoMapper.toHistoricProcessInstanceDto(hpi);
    }

    private HistoricTaskInstanceDto toHistoricTaskInstanceDto(HistoricTaskInstance hti) {
        return DtoMapper.toHistoricTaskInstanceDto(hti);
    }
}
package com.example.backend.service;

import com.example.backend.dto.HistoricProcessInstanceDto;
import com.example.backend.dto.HistoricTaskInstanceDto;
import java.util.List;
import java.util.Map;

public interface FlowableHistoryService {
    List<HistoricProcessInstanceDto> getProcessHistory(String processKey);
    List<HistoricProcessInstanceDto> getAllProcessHistory();
    HistoricProcessInstanceDto getHistoricProcessInstance(String processInstanceId);
    List<HistoricTaskInstanceDto> getTaskHistory(String processInstanceId);
    List<HistoricTaskInstanceDto> getTaskHistoryByUser(String user);
    List<HistoricTaskInstanceDto> getCompletedTasks();
    Map<String, Object> getProcessStatistics();
    Map<String, Object> getTaskStatistics();
    List<HistoricProcessInstanceDto> getProcessHistoryByDateRange(String startDate, String endDate);
}

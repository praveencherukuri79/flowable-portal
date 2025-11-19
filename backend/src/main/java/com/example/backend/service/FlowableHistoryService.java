package com.example.backend.service;

import com.example.backend.dto.ProcessInstanceDto;
import com.example.backend.dto.TaskDto;
import java.util.List;
import java.util.Map;

public interface FlowableHistoryService {
    List<ProcessInstanceDto> getProcessHistory(String processKey);
    List<ProcessInstanceDto> getAllProcessHistory();
    ProcessInstanceDto getHistoricProcessInstance(String processInstanceId);
    List<TaskDto> getTaskHistory(String processInstanceId);
    List<TaskDto> getTaskHistoryByUser(String user);
    List<TaskDto> getCompletedTasks();
    Map<String, Object> getProcessStatistics();
    Map<String, Object> getTaskStatistics();
    List<ProcessInstanceDto> getProcessHistoryByDateRange(String startDate, String endDate);
}

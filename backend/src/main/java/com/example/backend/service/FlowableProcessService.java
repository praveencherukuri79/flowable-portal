package com.example.backend.service;

import com.example.backend.dto.ProcessInstanceDto;
import com.example.backend.dto.TaskDto;
import java.util.List;
import java.util.Map;

public interface FlowableProcessService {
    ProcessInstanceDto startProcess(String processKey);
    ProcessInstanceDto startProcess(String processKey, Map<String, Object> variables);
    List<ProcessInstanceDto> getActiveProcessInstances();
    List<ProcessInstanceDto> getProcessInstancesByKey(String processKey);
    ProcessInstanceDto getProcessInstance(String processInstanceId);
    void suspendProcessInstance(String processInstanceId);
    void activateProcessInstance(String processInstanceId);
    void deleteProcessInstance(String processInstanceId, String reason);
    List<TaskDto> getTasksForUser(String user);
    Map<String, String> getProcessStatistics();
}
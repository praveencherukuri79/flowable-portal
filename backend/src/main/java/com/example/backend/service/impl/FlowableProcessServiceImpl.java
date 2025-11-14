package com.example.backend.service.impl;

import com.example.backend.dto.ProcessInstanceDto;
import com.example.backend.dto.TaskDto;
import com.example.backend.service.FlowableProcessService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FlowableProcessServiceImpl implements FlowableProcessService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Override
    public ProcessInstanceDto startProcess(String processKey) {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processKey);
        return toProcessInstanceDto(pi);
    }

    @Override
    public ProcessInstanceDto startProcess(String processKey, Map<String, Object> variables) {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processKey, variables);
        return toProcessInstanceDto(pi);
    }

    @Override
    public List<ProcessInstanceDto> getActiveProcessInstances() {
        return runtimeService.createProcessInstanceQuery().active().list()
                .stream().map(this::toProcessInstanceDto).collect(Collectors.toList());
    }

    @Override
    public List<ProcessInstanceDto> getProcessInstancesByKey(String processKey) {
        return runtimeService.createProcessInstanceQuery().processDefinitionKey(processKey).list()
                .stream().map(this::toProcessInstanceDto).collect(Collectors.toList());
    }

    @Override
    public ProcessInstanceDto getProcessInstance(String processInstanceId) {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        return pi != null ? toProcessInstanceDto(pi) : null;
    }

    @Override
    public void suspendProcessInstance(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    @Override
    public void activateProcessInstance(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    @Override
    public void deleteProcessInstance(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
    }

    @Override
    public List<TaskDto> getTasksForUser(String user) {
        return taskService.createTaskQuery().taskCandidateOrAssigned(user).list()
                .stream().map(this::toTaskDto).collect(Collectors.toList());
    }

    @Override
    public Map<String, String> getProcessStatistics() {
        long activeCount = runtimeService.createProcessInstanceQuery().active().count();
        long suspendedCount = runtimeService.createProcessInstanceQuery().suspended().count();
        return Map.of(
                "activeProcesses", String.valueOf(activeCount),
                "suspendedProcesses", String.valueOf(suspendedCount),
                "totalProcesses", String.valueOf(activeCount + suspendedCount)
        );
    }

    private ProcessInstanceDto toProcessInstanceDto(ProcessInstance pi) {
        ProcessInstanceDto dto = new ProcessInstanceDto();
        dto.id = pi.getId();
        dto.processDefinitionId = pi.getProcessDefinitionId();
        dto.processDefinitionKey = pi.getProcessDefinitionKey();
        dto.businessKey = pi.getBusinessKey();
        dto.startTime = pi.getStartTime() != null ? pi.getStartTime().toString() : null;
        dto.startUserId = pi.getStartUserId();
        dto.suspended = pi.isSuspended();
        dto.tenantId = pi.getTenantId();
        return dto;
    }

    private TaskDto toTaskDto(Task t) {
        TaskDto dto = new TaskDto();
        dto.id = t.getId();
        dto.name = t.getName();
        dto.description = t.getDescription();
        dto.assignee = t.getAssignee();
        dto.owner = t.getOwner();
        dto.delegationState = t.getDelegationState() != null ? t.getDelegationState().name() : null;
        dto.processInstanceId = t.getProcessInstanceId();
        dto.processDefinitionId = t.getProcessDefinitionId();
        dto.executionId = t.getExecutionId();
        dto.taskDefinitionKey = t.getTaskDefinitionKey();
        dto.createTime = t.getCreateTime();
        dto.dueDate = t.getDueDate();
        dto.priority = t.getPriority();
        dto.category = t.getCategory();
        dto.formKey = t.getFormKey();
        dto.tenantId = t.getTenantId();
        return dto;
    }
}
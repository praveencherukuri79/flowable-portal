package com.example.backend.service.impl;

import com.example.backend.dto.*;
import com.example.backend.service.AdminTaskService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminTaskServiceImpl implements AdminTaskService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Override
    public PagedResponse<TaskDto> searchTasks(String candidateGroup, String state, int page, int size) {
        var query = taskService.createTaskQuery();

        if (candidateGroup != null && !candidateGroup.isBlank()) {
            query.taskCandidateGroup(candidateGroup);
        }
        if ("CLAIMABLE".equalsIgnoreCase(state)) {
            query.taskUnassigned();
        }
        if ("ASSIGNED".equalsIgnoreCase(state)) {
            query.taskAssigned();
        }

        long total = query.count();
        List<Task> tasks = query.orderByTaskCreateTime().desc().listPage(page * size, size);

        List<TaskDto> dtos = tasks.stream()
                .map(t -> toTaskDto(t, getVariables(t.getProcessInstanceId())))
                .collect(Collectors.toList());

        PagedResponse<TaskDto> response = new PagedResponse<>();
        response.content = dtos;
        response.total = total;
        return response;
    }

    private Map<String, Object> getVariables(String processInstanceId) {
        if (processInstanceId == null) {
            return Collections.emptyMap();
        }
        List<?> variables = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        
        Map<String, Object> result = new HashMap<>();
        for (Object var : variables) {
            try {
                java.lang.reflect.Method getVariableName = var.getClass().getMethod("getVariableName");
                java.lang.reflect.Method getValue = var.getClass().getMethod("getValue");
                String name = (String) getVariableName.invoke(var);
                Object value = getValue.invoke(var);
                result.put(name, value);
            } catch (Exception e) {
                // Skip if reflection fails
            }
        }
        return result;
    }

    private TaskDto toTaskDto(Task t, Map<String, Object> vars) {
        TaskDto dto = new TaskDto();
        dto.id = t.getId();
        dto.name = t.getName();
        dto.description = t.getDescription();
        dto.assignee = t.getAssignee();
        dto.owner = t.getOwner();
        dto.delegationState = t.getDelegationState() != null ? t.getDelegationState().toString() : null;
        dto.processInstanceId = t.getProcessInstanceId();
        dto.processDefinitionId = t.getProcessDefinitionId();
        dto.executionId = t.getExecutionId();
        dto.taskDefinitionKey = t.getTaskDefinitionKey();
        dto.createTime = t.getCreateTime();
        dto.dueDate = t.getDueDate();
        dto.priority = t.getPriority();
        dto.category = t.getCategory();
        dto.formKey = t.getFormKey();
        dto.suspended = String.valueOf(t.isSuspended());
        dto.tenantId = t.getTenantId();
        dto.state = t.getAssignee() == null ? "CLAIMABLE" : "ASSIGNED";
        dto.variables = vars;
        return dto;
    }
}


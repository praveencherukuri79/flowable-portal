package com.example.backend.service.impl;

import com.example.backend.dto.TaskDto;
import com.example.backend.service.FlowableTaskService;
import org.flowable.task.api.Task;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlowableTaskServiceImpl implements FlowableTaskService {
    @Autowired
    private TaskService taskService;

    @Override
    public List<TaskDto> getTasksForUser(String user) {
        return taskService.createTaskQuery().taskCandidateOrAssigned(user).list().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public void claimTask(String taskId, String user) {
        taskService.claim(taskId, user);
    }

    @Override
    public void completeTask(String taskId, java.util.Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }

    @Override
    public void reassignTask(String taskId, String newUser) {
        taskService.setAssignee(taskId, newUser);
    }

    @Override
    public void delegateTask(String taskId, String delegateUser) {
        taskService.delegateTask(taskId, delegateUser);
    }

    private TaskDto toDto(Task t) {
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
        // Add more mappings as needed
        return dto;
    }
}

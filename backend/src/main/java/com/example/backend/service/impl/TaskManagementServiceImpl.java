package com.example.backend.service.impl;

import com.example.backend.dto.TaskActionResponse;
import com.example.backend.dto.TaskDto;
import com.example.backend.service.TaskManagementService;
import com.example.backend.util.DtoMapper;
import com.example.backend.util.FlowableQueryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of TaskManagementService.
 * Handles all business logic for task management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskManagementServiceImpl implements TaskManagementService {

    private final TaskService taskService;

    @Override
    public List<TaskDto> getMyTasks(String username) {
        log.info("Getting tasks for user: {}", username);
        List<Task> tasks = FlowableQueryUtils.getTasksForUser(taskService, username);
        return tasks.stream()
                .map(DtoMapper::toTaskDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDto> getTasksByCandidateGroup(String group) {
        log.info("Getting tasks for candidate group: {}", group);
        List<Task> tasks = FlowableQueryUtils.getTasksByCandidateGroup(taskService, group);
        return tasks.stream()
                .map(DtoMapper::toTaskDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDto> getTasksByGroupOrAssigned(String group, String username) {
        log.info("Getting tasks for group: {} or assigned to: {}", group, username);
        List<Task> tasks = FlowableQueryUtils.getTasksByGroupOrAssigned(taskService, group, username);
        return tasks.stream()
                .map(DtoMapper::toTaskDto)
                .collect(Collectors.toList());
    }

    @Override
    public TaskActionResponse claimTask(String taskId, String username) {
        log.info("User {} claiming task: {}", username, taskId);
        
        Task task = FlowableQueryUtils.getTaskOrThrow(taskService, taskId);
        
        if (task.getAssignee() != null) {
            throw new IllegalStateException("Task is already claimed by: " + task.getAssignee());
        }
        
        taskService.claim(taskId, username);
        
        return TaskActionResponse.builder()
                .taskId(taskId)
                .action("CLAIM")
                .performedBy(username)
                .message("Task claimed successfully")
                .taskState("ASSIGNED")
                .build();
    }

    @Override
    public TaskActionResponse unclaimTask(String taskId, String username) {
        log.info("User {} unclaiming task: {}", username, taskId);
        
        Task task = FlowableQueryUtils.getTaskOrThrow(taskService, taskId);
        
        if (!username.equals(task.getAssignee())) {
            throw new IllegalStateException("Task is not assigned to user: " + username);
        }
        
        taskService.unclaim(taskId);
        
        return TaskActionResponse.builder()
                .taskId(taskId)
                .action("UNCLAIM")
                .performedBy(username)
                .message("Task unclaimed successfully")
                .taskState("UNASSIGNED")
                .build();
    }

    @Override
    public TaskActionResponse reassignTask(String taskId, String newAssignee, String currentUser) {
        log.info("User {} reassigning task {} to {}", currentUser, taskId, newAssignee);
        
        FlowableQueryUtils.getTaskOrThrow(taskService, taskId);
        taskService.setAssignee(taskId, newAssignee);
        
        return TaskActionResponse.builder()
                .taskId(taskId)
                .action("REASSIGN")
                .performedBy(currentUser)
                .message("Task reassigned to " + newAssignee)
                .taskState("ASSIGNED")
                .build();
    }

    @Override
    public TaskActionResponse delegateTask(String taskId, String delegateUser, String currentUser) {
        log.info("User {} delegating task {} to {}", currentUser, taskId, delegateUser);
        
        FlowableQueryUtils.getTaskOrThrow(taskService, taskId);
        taskService.delegateTask(taskId, delegateUser);
        
        return TaskActionResponse.builder()
                .taskId(taskId)
                .action("DELEGATE")
                .performedBy(currentUser)
                .message("Task delegated to " + delegateUser)
                .taskState("DELEGATED")
                .build();
    }

    @Override
    public TaskActionResponse completeTask(String taskId, Map<String, Object> variables, String username) {
        log.info("User {} completing task: {}", username, taskId);
        
        FlowableQueryUtils.getTaskOrThrow(taskService, taskId);
        taskService.complete(taskId, variables);
        
        return TaskActionResponse.builder()
                .taskId(taskId)
                .action("COMPLETE")
                .performedBy(username)
                .message("Task completed successfully")
                .taskState("COMPLETED")
                .build();
    }

    @Override
    public Map<String, Object> getTaskVariables(String taskId) {
        log.info("Getting variables for task: {}", taskId);
        return taskService.getVariables(taskId);
    }

    @Override
    public void setTaskVariable(String taskId, String variableName, Object value) {
        log.info("Setting variable {} on task {}", variableName, taskId);
        taskService.setVariable(taskId, variableName, value);
    }

    @Override
    public void deleteTask(String taskId, String reason) {
        log.info("Deleting task {} with reason: {}", taskId, reason);
        taskService.deleteTask(taskId, reason);
    }
}


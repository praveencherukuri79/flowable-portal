package com.example.backend.service;

import com.example.backend.dto.TaskActionResponse;
import com.example.backend.dto.TaskDto;

import java.util.List;
import java.util.Map;

/**
 * Service for managing tasks.
 * Consolidates business logic for task operations.
 */
public interface TaskManagementService {

    /**
     * Get tasks for current user (assigned or claimable)
     */
    List<TaskDto> getMyTasks(String username);

    /**
     * Get tasks by candidate group
     */
    List<TaskDto> getTasksByCandidateGroup(String group);

    /**
     * Get tasks by candidate group OR assigned to user
     */
    List<TaskDto> getTasksByGroupOrAssigned(String group, String username);

    /**
     * Claim a task
     */
    TaskActionResponse claimTask(String taskId, String username);

    /**
     * Unclaim a task (set assignee to null)
     */
    TaskActionResponse unclaimTask(String taskId, String username);

    /**
     * Reassign a task to another user
     */
    TaskActionResponse reassignTask(String taskId, String newAssignee, String currentUser);

    /**
     * Delegate a task
     */
    TaskActionResponse delegateTask(String taskId, String delegateUser, String currentUser);

    /**
     * Complete a task with variables
     */
    TaskActionResponse completeTask(String taskId, Map<String, Object> variables, String username);

    /**
     * Get task variables
     */
    Map<String, Object> getTaskVariables(String taskId);

    /**
     * Set task variable
     */
    void setTaskVariable(String taskId, String variableName, Object value);

    /**
     * Delete a task
     */
    void deleteTask(String taskId, String reason);
}


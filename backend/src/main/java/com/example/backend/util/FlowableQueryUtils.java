package com.example.backend.util;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;

import java.util.List;

/**
 * Utility class for common Flowable query operations.
 * Reduces duplication across services and controllers.
 */
public class FlowableQueryUtils {

    /**
     * Get tasks for a user (assigned or candidate)
     */
    public static List<Task> getTasksForUser(TaskService taskService, String username) {
        return taskService.createTaskQuery()
                .or()
                    .taskAssignee(username)
                    .taskCandidateUser(username)
                .endOr()
                .list();
    }

    /**
     * Get tasks by candidate group
     */
    public static List<Task> getTasksByCandidateGroup(TaskService taskService, String group) {
        return taskService.createTaskQuery()
                .taskCandidateGroup(group)
                .list();
    }

    /**
     * Get tasks by candidate group OR assigned to user
     */
    public static List<Task> getTasksByGroupOrAssigned(TaskService taskService, String group, String username) {
        return taskService.createTaskQuery()
                .or()
                    .taskCandidateGroup(group)
                    .taskAssignee(username)
                .endOr()
                .list();
    }

    /**
     * Get assigned tasks for user
     */
    public static List<Task> getAssignedTasks(TaskService taskService, String username) {
        return taskService.createTaskQuery()
                .taskAssignee(username)
                .list();
    }

    /**
     * Get unassigned tasks for candidate group
     */
    public static List<Task> getUnassignedTasksForGroup(TaskService taskService, String group) {
        return taskService.createTaskQuery()
                .taskCandidateGroup(group)
                .taskUnassigned()
                .list();
    }

    /**
     * Check if task exists
     */
    public static boolean taskExists(TaskService taskService, String taskId) {
        return taskService.createTaskQuery()
                .taskId(taskId)
                .count() > 0;
    }

    /**
     * Check if user can claim task (is candidate or in candidate group)
     */
    public static boolean canUserClaimTask(TaskService taskService, String taskId, String username) {
        return taskService.createTaskQuery()
                .taskId(taskId)
                .or()
                    .taskCandidateUser(username)
                    .taskCandidateGroupIn(getUserGroups(username))
                .endOr()
                .count() > 0;
    }

    /**
     * Get task or throw exception
     */
    public static Task getTaskOrThrow(TaskService taskService, String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        
        if (task == null) {
            throw new IllegalArgumentException("Task with ID '" + taskId + "' not found");
        }
        
        return task;
    }

    /**
     * Get user groups (could be extended to query from security context)
     */
    private static List<String> getUserGroups(String username) {
        // Placeholder - could be implemented to get actual user groups
        return List.of();
    }
}


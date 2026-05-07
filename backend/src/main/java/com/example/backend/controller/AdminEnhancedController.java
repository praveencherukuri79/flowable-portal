package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.tenant.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced Admin APIs for the new Admin Dashboard.
 * Provides additional endpoints for comprehensive process management.
 */
@RestController
@RequestMapping("/api/admin/v2")
@Tag(name = "Admin Portal V2", description = "Enhanced APIs for the new Flowable Admin Dashboard")
public class AdminEnhancedController {

    private static final Logger log = LoggerFactory.getLogger(AdminEnhancedController.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    /**
     * Get comprehensive instance details including variables, tasks, and activities.
     */
    @Operation(summary = "Get comprehensive instance details",
            description = "Retrieves all details for a process instance including variables, tasks, and activity history")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved instance details"),
            @ApiResponse(responseCode = "404", description = "Process instance not found")
    })
    @GetMapping("/instances/{processInstanceId}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getInstanceDetails(
            @Parameter(description = "Process instance ID", required = true)
            @PathVariable String processInstanceId) {
        
        log.info("Fetching comprehensive details for instance: {}", processInstanceId);
        
        Map<String, Object> result = new HashMap<>();
        
        // Get historic process instance (works for both running and completed)
        HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
            .processInstanceTenantId(TenantContextHolder.getRequiredTenantId())
                .processInstanceId(processInstanceId)
                .singleResult();
        
        if (historicInstance == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Instance info
        Map<String, Object> instanceInfo = new HashMap<>();
        instanceInfo.put("id", historicInstance.getId());
        instanceInfo.put("definitionId", historicInstance.getProcessDefinitionId());
        instanceInfo.put("definitionKey", historicInstance.getProcessDefinitionKey());
        instanceInfo.put("definitionName", historicInstance.getProcessDefinitionName());
        instanceInfo.put("businessKey", historicInstance.getBusinessKey());
        instanceInfo.put("startUserId", historicInstance.getStartUserId());
        instanceInfo.put("startTime", historicInstance.getStartTime());
        instanceInfo.put("endTime", historicInstance.getEndTime());
        instanceInfo.put("durationInMillis", historicInstance.getDurationInMillis());
        instanceInfo.put("state", historicInstance.getEndTime() == null ? "RUNNING" : "COMPLETED");
        result.put("instance", instanceInfo);
        
        // Variables
        List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        
        List<Map<String, Object>> variablesList = variables.stream().map(v -> {
            Map<String, Object> var = new HashMap<>();
            var.put("name", v.getVariableName());
            var.put("type", v.getVariableTypeName());
            var.put("value", v.getValue());
            var.put("createTime", v.getCreateTime());
            var.put("lastUpdatedTime", v.getLastUpdatedTime());
            return var;
        }).collect(Collectors.toList());
        result.put("variables", variablesList);
        
        // Tasks (both active and historic)
        List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
            .taskTenantId(TenantContextHolder.getRequiredTenantId())
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceEndTime().desc()
                .list();
        
        List<Map<String, Object>> tasksList = historicTasks.stream().map(t -> {
            Map<String, Object> task = new HashMap<>();
            task.put("id", t.getId());
            task.put("name", t.getName());
            task.put("taskDefinitionKey", t.getTaskDefinitionKey());
            task.put("assignee", t.getAssignee());
            task.put("owner", t.getOwner());
            task.put("createTime", t.getCreateTime());
            task.put("claimTime", t.getClaimTime());
            task.put("endTime", t.getEndTime());
            task.put("dueDate", t.getDueDate());
            task.put("priority", t.getPriority());
            task.put("durationInMillis", t.getDurationInMillis());
            task.put("state", t.getEndTime() == null ? "ACTIVE" : "COMPLETED");
            return task;
        }).collect(Collectors.toList());
        result.put("tasks", tasksList);
        
        // Activities
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
        
        List<Map<String, Object>> activitiesList = activities.stream().map(a -> {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", a.getId());
            activity.put("activityId", a.getActivityId());
            activity.put("activityName", a.getActivityName());
            activity.put("activityType", a.getActivityType());
            activity.put("executionId", a.getExecutionId());
            activity.put("assignee", a.getAssignee());
            activity.put("taskId", a.getTaskId());
            activity.put("startTime", a.getStartTime());
            activity.put("endTime", a.getEndTime());
            activity.put("durationInMillis", a.getDurationInMillis());
            return activity;
        }).collect(Collectors.toList());
        result.put("activities", activitiesList);
        
        // Current activity (for running instances)
        if (historicInstance.getEndTime() == null) {
            List<Task> activeTasks = taskService.createTaskQuery()
                    .taskTenantId(TenantContextHolder.getRequiredTenantId())
                    .processInstanceId(processInstanceId)
                    .list();
            
            List<Map<String, Object>> activeTasksList = activeTasks.stream().map(t -> {
                Map<String, Object> task = new HashMap<>();
                task.put("id", t.getId());
                task.put("name", t.getName());
                task.put("taskDefinitionKey", t.getTaskDefinitionKey());
                task.put("assignee", t.getAssignee());
                task.put("owner", t.getOwner());
                task.put("createTime", t.getCreateTime());
                task.put("dueDate", t.getDueDate());
                task.put("priority", t.getPriority());
                task.put("candidateGroups", getCandidateGroups(t.getId()));
                return task;
            }).collect(Collectors.toList());
            result.put("activeTasks", activeTasksList);
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Force complete a task with variables.
     */
    @Operation(summary = "Force complete a task",
            description = "Admin action to force complete a task with optional outcome variables")
    @PostMapping("/tasks/{taskId}/force-complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> forceCompleteTask(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> variables,
            Principal principal) {
        
        log.info("Force completing task: {} by user: {}", taskId, principal.getName());
        
        Task task = taskService.createTaskQuery()
                .taskTenantId(TenantContextHolder.getRequiredTenantId())
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Set the admin as assignee if not assigned
        if (task.getAssignee() == null) {
            taskService.setAssignee(taskId, principal.getName());
        }
        
        // Complete the task
        if (variables != null && !variables.isEmpty()) {
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("taskId", taskId);
        response.put("message", "Task force completed successfully");
        response.put("completedBy", principal.getName());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Bulk action on multiple instances.
     */
    @Operation(summary = "Bulk action on instances",
            description = "Perform bulk actions (suspend, activate, delete) on multiple instances")
    @PostMapping("/instances/bulk-action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkInstanceAction(
            @RequestBody BulkActionRequest request,
            Principal principal) {
        
        log.info("Bulk action: {} on {} instances by user: {}", 
                request.getAction(), request.getInstanceIds().size(), principal.getName());
        
        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        
        for (String instanceId : request.getInstanceIds()) {
            try {
                switch (request.getAction().toUpperCase()) {
                    case "SUSPEND":
                        runtimeService.suspendProcessInstanceById(instanceId);
                        break;
                    case "ACTIVATE":
                        runtimeService.activateProcessInstanceById(instanceId);
                        break;
                    case "DELETE":
                        runtimeService.deleteProcessInstance(instanceId, 
                                request.getReason() != null ? request.getReason() : "Bulk deleted by admin");
                        break;
                    default:
                        failedIds.add(instanceId);
                        continue;
                }
                successIds.add(instanceId);
            } catch (Exception e) {
                log.error("Failed to perform {} on instance {}: {}", request.getAction(), instanceId, e.getMessage());
                failedIds.add(instanceId);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("action", request.getAction());
        response.put("successCount", successIds.size());
        response.put("failedCount", failedIds.size());
        response.put("successIds", successIds);
        response.put("failedIds", failedIds);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get dashboard statistics.
     */
    @Operation(summary = "Get dashboard statistics",
            description = "Retrieves comprehensive statistics for the admin dashboard")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        
        Map<String, Object> stats = new HashMap<>();
        
        // Running instances count
        long runningCount = historyService.createHistoricProcessInstanceQuery()
            .processInstanceTenantId(TenantContextHolder.getRequiredTenantId())
                .unfinished()
                .count();
        stats.put("runningInstances", runningCount);
        
        // Completed instances count
        long completedCount = historyService.createHistoricProcessInstanceQuery()
            .processInstanceTenantId(TenantContextHolder.getRequiredTenantId())
                .finished()
                .count();
        stats.put("completedInstances", completedCount);
        
        // Suspended instances count
        long suspendedCount = runtimeService.createProcessInstanceQuery()
            .processInstanceTenantId(TenantContextHolder.getRequiredTenantId())
                .suspended()
                .count();
        stats.put("suspendedInstances", suspendedCount);
        
        // Active tasks count
        long activeTasks = taskService.createTaskQuery()
            .taskTenantId(TenantContextHolder.getRequiredTenantId())
            .count();
        stats.put("activeTasks", activeTasks);
        
        // Overdue tasks count
        long overdueTasks = taskService.createTaskQuery()
            .taskTenantId(TenantContextHolder.getRequiredTenantId())
                .taskDueBefore(new Date())
                .count();
        stats.put("overdueTasks", overdueTasks);
        
        // Completed today
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        long completedToday = historyService.createHistoricProcessInstanceQuery()
            .processInstanceTenantId(TenantContextHolder.getRequiredTenantId())
                .finished()
                .finishedAfter(today.getTime())
                .count();
        stats.put("completedToday", completedToday);
        
        // Instances by day (last 7 days)
        List<Map<String, Object>> instancesByDay = new ArrayList<>();
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        
        for (int i = 6; i >= 0; i--) {
            Calendar startOfDay = Calendar.getInstance();
            startOfDay.add(Calendar.DAY_OF_MONTH, -i);
            startOfDay.set(Calendar.HOUR_OF_DAY, 0);
            startOfDay.set(Calendar.MINUTE, 0);
            startOfDay.set(Calendar.SECOND, 0);
            
            Calendar endOfDay = (Calendar) startOfDay.clone();
            endOfDay.add(Calendar.DAY_OF_MONTH, 1);
            
            long count = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceTenantId(TenantContextHolder.getRequiredTenantId())
                    .startedAfter(startOfDay.getTime())
                    .startedBefore(endOfDay.getTime())
                    .count();
            
            Map<String, Object> dayStats = new HashMap<>();
            dayStats.put("day", dayNames[startOfDay.get(Calendar.DAY_OF_WEEK) - 1]);
            dayStats.put("count", count);
            instancesByDay.add(dayStats);
        }
        stats.put("instancesByDay", instancesByDay);
        
        return ResponseEntity.ok(stats);
    }

    private List<String> getCandidateGroups(String taskId) {
        try {
            return taskService.getIdentityLinksForTask(taskId).stream()
                    .filter(link -> "candidate".equals(link.getType()) && link.getGroupId() != null)
                    .map(link -> link.getGroupId())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // Request DTOs
    public static class BulkActionRequest {
        private List<String> instanceIds;
        private String action;
        private String reason;

        public List<String> getInstanceIds() { return instanceIds; }
        public void setInstanceIds(List<String> instanceIds) { this.instanceIds = instanceIds; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}


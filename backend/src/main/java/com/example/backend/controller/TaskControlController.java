package com.example.backend.controller;

import com.example.backend.tenant.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task Control", description = "Generic task control APIs for admin")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class TaskControlController {
    
    private final TaskService taskService;
    
    @PostMapping("/{taskId}/assign")
    @Operation(summary = "Assign task", description = "Assign a task to a user")
    public ResponseEntity<Map<String, String>> assignTask(
            @PathVariable String taskId,
            @RequestParam String userId
    ) {
        log.info(">>> Admin assigning task {} to user {}", taskId, userId);
        requireTask(taskId);
        taskService.setAssignee(taskId, userId);
        log.info(">>> Task assigned successfully");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Task assigned successfully");
        response.put("taskId", taskId);
        response.put("assignee", userId);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{taskId}/unclaim")
    @Operation(summary = "Unclaim task", description = "Remove assignee from a task")
    public ResponseEntity<Map<String, String>> unclaimTask(@PathVariable String taskId) {
        requireTask(taskId);
        taskService.unclaim(taskId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Task unclaimed successfully");
        response.put("taskId", taskId);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{taskId}/complete")
    @Operation(summary = "Complete task", description = "Complete a task with optional variables")
    public ResponseEntity<Map<String, String>> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> variables
    ) {
        log.info(">>> Admin completing task {} with variables: {}", taskId, variables);
        requireTask(taskId);
        if (variables != null && !variables.isEmpty()) {
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }
        log.info(">>> Task completed successfully");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Task completed successfully");
        response.put("taskId", taskId);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete task", description = "Delete a task")
    public ResponseEntity<Map<String, String>> deleteTask(
            @PathVariable String taskId,
            @RequestParam(required = false) String deleteReason
    ) {
        requireTask(taskId);
        taskService.deleteTask(taskId, deleteReason != null ? deleteReason : "Deleted by admin");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Task deleted successfully");
        response.put("taskId", taskId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{taskId}/variables")
    @Operation(summary = "Get task variables", description = "Get all variables for a task")
    public ResponseEntity<Map<String, Object>> getTaskVariables(@PathVariable String taskId) {
        requireTask(taskId);
        Map<String, Object> variables = taskService.getVariables(taskId);
        return ResponseEntity.ok(variables);
    }
    
    @PutMapping("/{taskId}/variables")
    @Operation(summary = "Update task variables", description = "Update variables for a task")
    public ResponseEntity<Map<String, String>> updateTaskVariables(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> variables
    ) {
        requireTask(taskId);
        taskService.setVariables(taskId, variables);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Task variables updated successfully");
        response.put("taskId", taskId);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{taskId}/set-priority")
    @Operation(summary = "Set task priority", description = "Set priority for a task")
    public ResponseEntity<Map<String, String>> setTaskPriority(
            @PathVariable String taskId,
            @RequestParam int priority
    ) {
        requireTask(taskId);
        taskService.setPriority(taskId, priority);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Task priority updated");
        response.put("taskId", taskId);
        response.put("priority", String.valueOf(priority));
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{taskId}/set-due-date")
    @Operation(summary = "Set task due date", description = "Set due date for a task")
    public ResponseEntity<Map<String, String>> setTaskDueDate(
            @PathVariable String taskId,
            @RequestParam String dueDate
    ) {
        try {
            requireTask(taskId);
            java.util.Date date = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(dueDate);
            taskService.setDueDate(taskId, date);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Task due date updated");
            response.put("taskId", taskId);
            response.put("dueDate", dueDate);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid date format. Use yyyy-MM-dd");
            return ResponseEntity.badRequest().body(response);
        }
    }

    private Task requireTask(String taskId) {
        Task task = taskService.createTaskQuery()
                .taskTenantId(TenantContextHolder.getRequiredTenantId())
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new FlowableObjectNotFoundException("Task not found for tenant: " + taskId, Task.class);
        }
        return task;
    }
}


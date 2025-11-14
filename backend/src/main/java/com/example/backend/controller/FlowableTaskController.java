package com.example.backend.controller;

import com.example.backend.service.FlowableTaskService;
import com.example.backend.dto.TaskDto;
import com.example.backend.dto.TaskActionResponse;
import com.example.backend.util.DtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic controller for Flowable Task operations.
 * Provides endpoints for task management: list, assign, reassign, delegate, complete.
 */
@RestController
@RequestMapping("/api/flowable/task")
@RequiredArgsConstructor
@Tag(name = "Flowable Task", description = "Generic Flowable task APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class FlowableTaskController {
    
    private final FlowableTaskService flowableTaskService;
    private final TaskService taskService;

    @Operation(summary = "Get tasks for user", 
               description = "Retrieve all tasks that are assigned to or can be claimed by the specified user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks"),
        @ApiResponse(responseCode = "400", description = "Invalid user parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user/{user}")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public List<TaskDto> getTasksForUser(
            @Parameter(description = "Username to get tasks for", required = true) 
            @PathVariable String user) {
        return flowableTaskService.getTasksForUser(user);
    }
    
    @GetMapping("/candidate-group/{group}")
    @Operation(summary = "Get tasks by candidate group", description = "Get all tasks for a specific candidate group")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<List<TaskDto>> getTasksByCandidateGroup(@PathVariable String group) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateGroup(group)
                .list();
        
        List<TaskDto> dtos = tasks.stream()
                .map(DtoMapper::toTaskDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/my-tasks")
    @Operation(summary = "Get current user's tasks", description = "Get all tasks for current authenticated user")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<List<TaskDto>> getMyTasks(Principal principal) {
        List<Task> tasks = taskService.createTaskQuery()
                .or()
                    .taskAssignee(principal.getName())
                    .taskCandidateUser(principal.getName())
                .endOr()
                .list();
        
        List<TaskDto> dtos = tasks.stream()
                .map(DtoMapper::toTaskDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/candidate-group-or-assigned/{group}")
    @Operation(summary = "Get tasks by group or assigned", description = "Get tasks for a candidate group OR assigned to user")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<List<TaskDto>> getTasksByGroupOrAssigned(
            @PathVariable String group,
            Principal principal
    ) {
        List<Task> tasks = taskService.createTaskQuery()
                .or()
                    .taskCandidateGroup(group)
                    .taskAssignee(principal.getName())
                .endOr()
                .list();
        
        List<TaskDto> dtos = tasks.stream()
                .map(DtoMapper::toTaskDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{taskId}/variables")
    @Operation(summary = "Get task variables", description = "Get all variables for a task")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getTaskVariables(@PathVariable String taskId) {
        Map<String, Object> variables = taskService.getVariables(taskId);
        return ResponseEntity.ok(variables);
    }

    @Operation(summary = "Assign task to user", 
               description = "Claim/assign a task to a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task successfully assigned"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    @PostMapping("/assign/{taskId}")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<TaskActionResponse> claimTask(
            @Parameter(description = "Task ID to assign", required = true) 
            @PathVariable String taskId, 
            @Parameter(description = "Username to assign task to", required = true) 
            @RequestParam String user) {
        flowableTaskService.claimTask(taskId, user);
        
        TaskActionResponse response = TaskActionResponse.builder()
                .taskId(taskId)
                .action("ASSIGNED")
                .performedBy(user)
                .message("Task assigned successfully")
                .taskState("ASSIGNED")
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{taskId}/claim")
    @Operation(summary = "Claim task", description = "Claim a task for current user")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<TaskActionResponse> claimTaskForCurrentUser(
            @PathVariable String taskId,
            Principal principal
    ) {
        taskService.claim(taskId, principal.getName());
        
        TaskActionResponse response = TaskActionResponse.builder()
                .taskId(taskId)
                .action("CLAIMED")
                .performedBy(principal.getName())
                .message("Task claimed successfully")
                .taskState("ASSIGNED")
                .build();
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reassign task", 
               description = "Reassign a task to a different user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task successfully reassigned"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PostMapping("/reassign/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskActionResponse> reassignTask(
            @Parameter(description = "Task ID to reassign", required = true) 
            @PathVariable String taskId, 
            @Parameter(description = "New assignee username", required = true) 
            @RequestParam String newUser) {
        flowableTaskService.reassignTask(taskId, newUser);
        
        TaskActionResponse response = TaskActionResponse.builder()
                .taskId(taskId)
                .action("REASSIGNED")
                .performedBy(newUser)
                .message("Task reassigned successfully")
                .taskState("ASSIGNED")
                .build();
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delegate task", 
               description = "Delegate a task to another user while keeping ownership")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task successfully delegated"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PostMapping("/delegate/{taskId}")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<TaskActionResponse> delegateTask(
            @Parameter(description = "Task ID to delegate", required = true) 
            @PathVariable String taskId, 
            @Parameter(description = "Username to delegate task to", required = true) 
            @RequestParam String delegateUser) {
        flowableTaskService.delegateTask(taskId, delegateUser);
        
        TaskActionResponse response = TaskActionResponse.builder()
                .taskId(taskId)
                .action("DELEGATED")
                .performedBy(delegateUser)
                .message("Task delegated successfully")
                .taskState("DELEGATED")
                .build();
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Complete task", 
               description = "Complete a task with process variables")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task completed successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "400", description = "Invalid task state or variables")
    })
    @PostMapping("/complete/{taskId}")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<TaskActionResponse> completeTask(
            @Parameter(description = "Task ID to complete", required = true) 
            @PathVariable String taskId, 
            @Parameter(description = "Process variables for task completion") 
            @RequestBody(required = false) Map<String, Object> variables,
            Principal principal
    ) {
        // Claim task if not already claimed
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null && task.getAssignee() == null) {
            taskService.claim(taskId, principal.getName());
        }
        
        if (variables == null) {
            variables = new HashMap<>();
        }
        
        // Add completion info
        variables.put("completedBy", principal.getName());
        variables.put("completedAt", new java.util.Date().toString());
        
        flowableTaskService.completeTask(taskId, variables);
        
        TaskActionResponse response = TaskActionResponse.builder()
                .taskId(taskId)
                .action("COMPLETED")
                .performedBy(principal.getName())
                .message("Task completed successfully")
                .taskState("COMPLETED")
                .build();
        
        return ResponseEntity.ok(response);
    }
}

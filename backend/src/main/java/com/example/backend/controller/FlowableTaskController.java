package com.example.backend.controller;

import com.example.backend.dto.TaskActionResponse;
import com.example.backend.dto.TaskDto;
import com.example.backend.service.TaskManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Clean controller for Flowable Task operations.
 * Business logic delegated to TaskManagementService.
 */
@RestController
@RequestMapping("/api/flowable/task")
@RequiredArgsConstructor
@Tag(name = "Flowable Task", description = "Generic Flowable task APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class FlowableTaskController {
    
    private final TaskManagementService taskManagementService;

    @GetMapping("/my-tasks")
    @Operation(summary = "Get current user's tasks", description = "Get all tasks for current authenticated user")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<List<TaskDto>> getMyTasks(Principal principal) {
        List<TaskDto> tasks = taskManagementService.getMyTasks(principal.getName());
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/candidate-group/{group}")
    @Operation(summary = "Get tasks by candidate group", description = "Get all tasks for a specific candidate group")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<List<TaskDto>> getTasksByCandidateGroup(@PathVariable String group) {
        List<TaskDto> tasks = taskManagementService.getTasksByCandidateGroup(group);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/candidate-group-or-assigned/{group}")
    @Operation(summary = "Get tasks by group or assigned", description = "Get tasks for a candidate group OR assigned to user")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<List<TaskDto>> getTasksByGroupOrAssigned(
            @PathVariable String group,
            Principal principal
    ) {
        List<TaskDto> tasks = taskManagementService.getTasksByGroupOrAssigned(group, principal.getName());
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/{taskId}/variables")
    @Operation(summary = "Get task variables", description = "Get all variables for a task")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getTaskVariables(@PathVariable String taskId) {
        Map<String, Object> variables = taskManagementService.getTaskVariables(taskId);
        return ResponseEntity.ok(variables);
    }

    @PostMapping("/{taskId}/claim")
    @Operation(summary = "Claim task", description = "Claim a task for current user")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<TaskActionResponse> claimTask(
            @PathVariable String taskId,
            Principal principal
    ) {
        TaskActionResponse response = taskManagementService.claimTask(taskId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/unclaim")
    @Operation(summary = "Unclaim task", description = "Unclaim/release a task")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<TaskActionResponse> unclaimTask(
            @PathVariable String taskId,
            Principal principal
    ) {
        TaskActionResponse response = taskManagementService.unclaimTask(taskId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reassign/{taskId}")
    @Operation(summary = "Reassign task", description = "Reassign a task to a different user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task successfully reassigned"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskActionResponse> reassignTask(
            @Parameter(description = "Task ID to reassign", required = true) 
            @PathVariable String taskId, 
            @Parameter(description = "New assignee username", required = true) 
            @RequestParam String newUser,
            Principal principal
    ) {
        TaskActionResponse response = taskManagementService.reassignTask(taskId, newUser, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delegate/{taskId}")
    @Operation(summary = "Delegate task", description = "Delegate a task to another user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskActionResponse> delegateTask(
            @PathVariable String taskId,
            @RequestParam String delegateUser,
            Principal principal
    ) {
        TaskActionResponse response = taskManagementService.delegateTask(taskId, delegateUser, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete/{taskId}")
    @Operation(summary = "Complete task", description = "Complete a task with optional variables")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<TaskActionResponse> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> variables,
            Principal principal
    ) {
        TaskActionResponse response = taskManagementService.completeTask(taskId, variables, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete task", description = "Delete a task")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(
            @PathVariable String taskId,
            @RequestParam(required = false, defaultValue = "Deleted by admin") String reason
    ) {
        taskManagementService.deleteTask(taskId, reason);
        return ResponseEntity.ok().build();
    }
}

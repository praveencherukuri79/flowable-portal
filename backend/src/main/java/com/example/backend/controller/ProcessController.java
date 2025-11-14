package com.example.backend.controller;

import com.example.backend.dto.ProcessInstanceDto;
import com.example.backend.dto.TaskDto;
import com.example.backend.service.FlowableProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for Flowable Process operations (maker-checker workflow).
 * Provides comprehensive process management APIs with verbose DTOs.
 */
@RestController
@RequestMapping("/api/process")
@Tag(name = "Process Management", description = "APIs for managing Flowable process instances and maker-checker workflows")
public class ProcessController {

    @Autowired
    private FlowableProcessService processService;

    /**
     * Start a new retention offer process.
     * @return ProcessInstanceDto with process details
     */
    @Operation(
        summary = "Start Retention Offer Process",
        description = "Initiates a new retention offer maker-checker workflow process"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Process started successfully",
            content = @Content(schema = @Schema(implementation = ProcessInstanceDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid process configuration"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/start")
    public ProcessInstanceDto startProcess() {
        return processService.startProcess("retentionOfferProcess");
    }

    /**
     * Start a process with variables.
     * @param processKey process definition key
     * @param variables initial process variables
     * @return ProcessInstanceDto with process details
     */
    @Operation(
        summary = "Start Process with Variables",
        description = "Initiates a new process instance with custom variables and process definition key"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Process started successfully with variables",
            content = @Content(schema = @Schema(implementation = ProcessInstanceDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid process key or variables"),
        @ApiResponse(responseCode = "404", description = "Process definition not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/start/{processKey}")
    public ProcessInstanceDto startProcessWithVariables(
            @Parameter(description = "Process definition key", required = true, example = "retentionOfferProcess")
            @PathVariable String processKey, 
            @Parameter(description = "Initial process variables", required = true)
            @RequestBody Map<String, Object> variables) {
        return processService.startProcess(processKey, variables);
    }

    /**
     * Get all active process instances.
     * @return list of active ProcessInstanceDto
     */
    @Operation(
        summary = "Get Active Processes",
        description = "Retrieves all currently active process instances across all process definitions"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved active processes",
            content = @Content(schema = @Schema(implementation = ProcessInstanceDto.class))
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/active")
    public List<ProcessInstanceDto> getActiveProcesses() {
        return processService.getActiveProcessInstances();
    }

    /**
     * Get process instances by process definition key.
     * @param processKey process definition key
     * @return list of ProcessInstanceDto
     */
    @Operation(
        summary = "Get Processes by Key",
        description = "Retrieves all process instances for a specific process definition key"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved processes by key",
            content = @Content(schema = @Schema(implementation = ProcessInstanceDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Process definition not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/by-key/{processKey}")
    public List<ProcessInstanceDto> getProcessesByKey(
            @Parameter(description = "Process definition key", required = true, example = "retentionOffer")
            @PathVariable String processKey) {
        return processService.getProcessInstancesByKey(processKey);
    }

    /**
     * Get a specific process instance.
     * @param processInstanceId process instance id
     * @return ProcessInstanceDto or null
     */
    @Operation(
        summary = "Get Process Instance",
        description = "Retrieves detailed information about a specific process instance by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved process instance",
            content = @Content(schema = @Schema(implementation = ProcessInstanceDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Process instance not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{processInstanceId}")
    public ProcessInstanceDto getProcessInstance(
            @Parameter(description = "Process instance ID", required = true, example = "12345")
            @PathVariable String processInstanceId) {
        return processService.getProcessInstance(processInstanceId);
    }

    /**
     * Suspend a process instance.
     * @param processInstanceId process instance id
     * @return status message
     */
    @Operation(
        summary = "Suspend Process Instance",
        description = "Suspends an active process instance, preventing further execution until activated"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Process instance suspended successfully"
        ),
        @ApiResponse(responseCode = "404", description = "Process instance not found"),
        @ApiResponse(responseCode = "400", description = "Process instance already suspended"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/suspend/{processInstanceId}")
    public Map<String, String> suspendProcess(
            @Parameter(description = "Process instance ID to suspend", required = true, example = "12345")
            @PathVariable String processInstanceId) {
        processService.suspendProcessInstance(processInstanceId);
        return Map.of("status", "suspended");
    }

    /**
     * Activate a suspended process instance.
     * @param processInstanceId process instance id
     * @return status message
     */
    @Operation(
        summary = "Activate Process Instance",
        description = "Activates a suspended process instance, resuming its execution"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Process instance activated successfully"
        ),
        @ApiResponse(responseCode = "404", description = "Process instance not found"),
        @ApiResponse(responseCode = "400", description = "Process instance is not suspended"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/activate/{processInstanceId}")
    public Map<String, String> activateProcess(
            @Parameter(description = "Process instance ID to activate", required = true, example = "12345")
            @PathVariable String processInstanceId) {
        processService.activateProcessInstance(processInstanceId);
        return Map.of("status", "activated");
    }

    /**
     * Delete a process instance.
     * @param processInstanceId process instance id
     * @param reason deletion reason
     * @return status message
     */
    @Operation(
        summary = "Delete Process Instance",
        description = "Permanently deletes a process instance with an optional reason"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Process instance deleted successfully"
        ),
        @ApiResponse(responseCode = "404", description = "Process instance not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{processInstanceId}")
    public Map<String, String> deleteProcess(
            @Parameter(description = "Process instance ID to delete", required = true, example = "12345")
            @PathVariable String processInstanceId, 
            @Parameter(description = "Reason for deletion", required = false, example = "Cancelled by user")
            @RequestParam(required = false) String reason) {
        processService.deleteProcessInstance(processInstanceId, reason != null ? reason : "Deleted by user");
        return Map.of("status", "deleted");
    }

    /**
     * Get tasks for a user.
     * @param user username
     * @return list of TaskDto
     */
    @Operation(
        summary = "Get User Tasks",
        description = "Retrieves all tasks assigned to or available for a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved user tasks",
            content = @Content(schema = @Schema(implementation = TaskDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid username"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/tasks")
    public List<TaskDto> getTasks(
            @Parameter(description = "Username to get tasks for", required = true, example = "john.doe")
            @RequestParam String user) {
        return processService.getTasksForUser(user);
    }

    /**
     * Get process statistics.
     * @return statistics map
     */
    @Operation(
        summary = "Get Process Statistics",
        description = "Retrieves comprehensive statistics about process instances and execution metrics"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved process statistics"
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/statistics")
    public Map<String, String> getStatistics() {
        return processService.getProcessStatistics();
    }
}

package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.AdminMetricsService;
import com.example.backend.service.AdminRuntimeService;
import com.example.backend.service.AdminTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Portal", description = "APIs for Flowable Admin Portal - dashboard, definitions, instances, tasks, events, and diagrams")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminRuntimeService runtimeService;

    @Autowired
    private AdminTaskService taskService;

    @Autowired
    private AdminMetricsService metricsService;

    @Operation(
        summary = "Get all deployed process definitions",
        description = "Retrieves all latest version process definitions for the admin portal"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved process definitions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/definitions")
    public List<ProcessDefinitionDto> listDefinitions() {
        log.info("Fetching all process definitions");
        return runtimeService.getProcessDefinitions();
    }

    @Operation(
        summary = "Search process instances",
        description = "Search and paginate process instances with optional filters by definition key and state"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved process instances"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/instances/search")
    public PagedResponse<ProcessInstanceDto> searchInstances(
            @Parameter(description = "Process definition key filter", required = false)
            @RequestParam(required = false) String definitionKey,
            @Parameter(description = "State filter (RUNNING or COMPLETED)", required = false)
            @RequestParam(required = false) String state,
            @Parameter(description = "Page number (0-based)", required = false)
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false)
            @RequestParam(defaultValue = "25") int size) {
        log.info("Searching process instances - definitionKey: {}, state: {}, page: {}, size: {}", definitionKey, state, page, size);
        return runtimeService.searchProcessInstances(definitionKey, state, page, size);
    }

    @Operation(
        summary = "Search tasks",
        description = "Search and paginate tasks with optional filters by candidate group and state"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/tasks/search")
    public PagedResponse<TaskDto> searchTasks(
            @Parameter(description = "Candidate group filter", required = false)
            @RequestParam(required = false) String candidateGroup,
            @Parameter(description = "State filter (CLAIMABLE or ASSIGNED)", required = false)
            @RequestParam(required = false) String state,
            @Parameter(description = "Page number (0-based)", required = false)
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false)
            @RequestParam(defaultValue = "25") int size) {
        log.info("Searching tasks - candidateGroup: {}, state: {}, page: {}, size: {}", candidateGroup, state, page, size);
        return taskService.searchTasks(candidateGroup, state, page, size);
    }

    @Operation(
        summary = "Fetch event logs",
        description = "Retrieves the most recent event log entries from Flowable"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved event logs"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/events/search")
    public List<EventLogDto> listEvents(
            @Parameter(description = "Maximum number of events to retrieve", required = false)
            @RequestParam(defaultValue = "100") int limit) {
        log.info("Fetching last {} event logs", limit);
        return runtimeService.getEventLogs(limit);
    }

    @Operation(
        summary = "Get dashboard metrics",
        description = "Retrieves comprehensive metrics for the admin portal dashboard including instance counts, task statistics, and daily trends"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved metrics"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/metrics")
    public MetricsDto getMetrics() {
        log.info("Building dashboard metrics");
        return metricsService.getMetrics();
    }

    @Operation(
        summary = "Generate runtime process diagram",
        description = "Generates an SVG diagram for a running process instance"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully generated diagram"),
        @ApiResponse(responseCode = "404", description = "Process instance not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/diagram/{processInstanceId}", produces = "image/svg+xml")
    public String getDiagram(
            @Parameter(description = "Process instance ID", required = true)
            @PathVariable String processInstanceId) {
        log.info("Generating process diagram for instance: {}", processInstanceId);
        return runtimeService.generateProcessDiagramSvg(processInstanceId);
    }
}


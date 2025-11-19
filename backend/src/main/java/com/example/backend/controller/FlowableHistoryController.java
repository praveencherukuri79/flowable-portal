package com.example.backend.controller;

import com.example.backend.dto.ProcessInstanceDto;
import com.example.backend.dto.TaskDto;
import com.example.backend.service.FlowableHistoryService;
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
 * Controller for Flowable History operations.
 * Provides comprehensive history APIs for processes and tasks with verbose DTOs.
 */
@RestController
@RequestMapping("/api/flowable/history")
@Tag(name = "History Management", description = "APIs for accessing Flowable process and task history")
public class FlowableHistoryController {

    @Autowired
    private FlowableHistoryService historyService;

    /**
     * Get process history by process definition key.
     * @param processKey process definition key
     * @return list of ProcessInstanceDto (consolidated DTO for both active and historic instances)
     */
    @Operation(
        summary = "Get Process History by Key",
        description = "Retrieves historical process instances for a specific process definition key"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved process history",
            content = @Content(schema = @Schema(implementation = ProcessInstanceDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Process definition not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/process/{processKey}")
    public List<ProcessInstanceDto> getProcessHistory(
            @Parameter(description = "Process definition key", required = true, example = "retentionOffer")
            @PathVariable String processKey) {
        return historyService.getProcessHistory(processKey);
    }

    /**
     * Get all process history.
     * @return list of ProcessInstanceDto (consolidated DTO for both active and historic instances)
     */
    @Operation(
        summary = "Get All Process History",
        description = "Retrieves all historical process instances across all process definitions"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved all process history",
            content = @Content(schema = @Schema(implementation = ProcessInstanceDto.class))
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/process")
    public List<ProcessInstanceDto> getAllProcessHistory() {
        return historyService.getAllProcessHistory();
    }

    /**
     * Get specific historic process instance.
     * @param processInstanceId process instance id
     * @return ProcessInstanceDto (consolidated DTO for both active and historic instances)
     */
    @GetMapping("/process/instance/{processInstanceId}")
    public ProcessInstanceDto getHistoricProcessInstance(@PathVariable String processInstanceId) {
        return historyService.getHistoricProcessInstance(processInstanceId);
    }

    /**
     * Get task history for a process instance.
     * @param processInstanceId process instance id
     * @return list of TaskDto (consolidated DTO for both active and historic tasks)
     */
    @GetMapping("/task/process/{processInstanceId}")
    public List<TaskDto> getTaskHistory(@PathVariable String processInstanceId) {
        return historyService.getTaskHistory(processInstanceId);
    }

    /**
     * Get task history for a user.
     * @param user username
     * @return list of TaskDto (consolidated DTO for both active and historic tasks)
     */
    @GetMapping("/task/user/{user}")
    public List<TaskDto> getTaskHistoryByUser(@PathVariable String user) {
        return historyService.getTaskHistoryByUser(user);
    }

    /**
     * Get all completed tasks.
     * @return list of TaskDto (consolidated DTO for both active and historic tasks)
     */
    @GetMapping("/task/completed")
    public List<TaskDto> getCompletedTasks() {
        return historyService.getCompletedTasks();
    }

    /**
     * Get process statistics.
     * @return statistics map
     */
    @GetMapping("/process/statistics")
    public Map<String, Object> getProcessStatistics() {
        return historyService.getProcessStatistics();
    }

    /**
     * Get task statistics.
     * @return statistics map
     */
    @GetMapping("/task/statistics")
    public Map<String, Object> getTaskStatistics() {
        return historyService.getTaskStatistics();
    }

    /**
     * Get process history by date range.
     * @param startDate start date (yyyy-MM-dd)
     * @param endDate end date (yyyy-MM-dd)
     * @return list of ProcessInstanceDto (consolidated DTO for both active and historic instances)
     */
    @GetMapping("/process/date-range")
    public List<ProcessInstanceDto> getProcessHistoryByDateRange(
            @RequestParam String startDate, @RequestParam String endDate) {
        return historyService.getProcessHistoryByDateRange(startDate, endDate);
    }
}

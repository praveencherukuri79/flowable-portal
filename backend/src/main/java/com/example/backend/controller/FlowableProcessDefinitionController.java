package com.example.backend.controller;

import com.example.backend.dto.ProcessDefinitionDto;
import com.example.backend.service.FlowableProcessDefinitionService;
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

/**
 * Controller for Flowable Process Definition operations.
 * Provides APIs to retrieve process definition metadata (templates).
 */
@RestController
@RequestMapping("/api/flowable/process-definition")
@CrossOrigin(origins = "http://localhost:3002", maxAge = 3600, allowCredentials = "true")
@Tag(name = "Process Definition Management", description = "APIs for retrieving process definition templates and metadata")
public class FlowableProcessDefinitionController {

    @Autowired
    private FlowableProcessDefinitionService processDefinitionService;

    /**
     * Get all process definitions.
     * @return list of ProcessDefinitionDto
     */
    @Operation(
        summary = "Get All Process Definitions",
        description = "Retrieves all process definitions (all versions) ordered by key and version"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved process definitions",
            content = @Content(schema = @Schema(implementation = ProcessDefinitionDto.class))
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public List<ProcessDefinitionDto> getAllProcessDefinitions() {
        return processDefinitionService.getAllProcessDefinitions();
    }

    /**
     * Get latest process definitions (one per key).
     * @return list of latest ProcessDefinitionDto
     */
    @Operation(
        summary = "Get Latest Process Definitions",
        description = "Retrieves the latest version of each process definition (one per key)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved latest process definitions",
            content = @Content(schema = @Schema(implementation = ProcessDefinitionDto.class))
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/latest")
    public List<ProcessDefinitionDto> getLatestProcessDefinitions() {
        return processDefinitionService.getLatestProcessDefinitions();
    }

    /**
     * Get process definition by key (latest version).
     * @param key process definition key
     * @return ProcessDefinitionDto or 404
     */
    @Operation(
        summary = "Get Process Definition by Key",
        description = "Retrieves the latest version of a process definition by its key"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved process definition",
            content = @Content(schema = @Schema(implementation = ProcessDefinitionDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Process definition not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/key/{key}")
    public ProcessDefinitionDto getProcessDefinitionByKey(
            @Parameter(description = "Process definition key", required = true, example = "retentionOfferProcess")
            @PathVariable String key) {
        return processDefinitionService.getProcessDefinitionByKey(key);
    }

    /**
     * Get process definition by ID.
     * @param id process definition id
     * @return ProcessDefinitionDto or 404
     */
    @Operation(
        summary = "Get Process Definition by ID",
        description = "Retrieves a specific process definition by its unique ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved process definition",
            content = @Content(schema = @Schema(implementation = ProcessDefinitionDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Process definition not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ProcessDefinitionDto getProcessDefinitionById(
            @Parameter(description = "Process definition ID", required = true)
            @PathVariable String id) {
        return processDefinitionService.getProcessDefinitionById(id);
    }
}

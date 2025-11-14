package com.example.backend.controller;

import com.example.backend.dto.ModelDto;
import com.example.backend.service.FlowableModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing Flowable models
 */
@RestController
@RequestMapping("/api/models")
@Tag(name = "Model Management", description = "Operations for managing Flowable process models")
public class FlowableModelController {

    @Autowired
    private FlowableModelService modelService;

    /**
     * Get all models with pagination support.
     * @return List of ModelDto
     */
    @Operation(
            summary = "Get all models",
            description = "Retrieve a list of all process models with pagination support"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Models retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ModelDto.class)))
    })
    @GetMapping
    @Cacheable("models")
    public List<ModelDto> getAllModels() {
        return modelService.getAllModels();
    }

    @Operation(
            summary = "Get a specific model by ID",
            description = "Retrieve detailed information about a specific model using its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Model retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Model not found")
    })
    @GetMapping("/{modelId}")
    @Cacheable(value = "model", key = "#modelId")
    public ModelDto getModel(
            @Parameter(description = "Model ID", example = "model-123")
            @PathVariable String modelId) {
        return modelService.getModel(modelId);
    }

    @Operation(
            summary = "Create a new model",
            description = "Create a new process model with the specified parameters"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Model created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    })
    @PostMapping
    @CacheEvict(value = "models", allEntries = true)
    public ModelDto createModel(
            @Parameter(description = "Model name", example = "Leave Request Process")
            @RequestParam String name,
            @Parameter(description = "Model key", example = "leaveRequest")
            @RequestParam String key,
            @Parameter(description = "Model description", example = "Process for handling employee leave requests")
            @RequestParam(required = false) String description) {
        return modelService.createModel(name, key, description);
    }

    @Operation(
            summary = "Update an existing model",
            description = "Update the properties of an existing model"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Model updated successfully"),
            @ApiResponse(responseCode = "404", description = "Model not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    })
    @PutMapping("/{modelId}")
    @CacheEvict(value = {"models", "model"}, key = "#modelId", allEntries = true)
    public ModelDto updateModel(
            @Parameter(description = "Model ID", example = "model-123")
            @PathVariable String modelId,
            @Parameter(description = "Model name", example = "Updated Leave Request Process")
            @RequestParam String name,
            @Parameter(description = "Model key", example = "updatedLeaveRequest")
            @RequestParam String key,
            @Parameter(description = "Model description", example = "Updated process for handling employee leave requests")
            @RequestParam(required = false) String description) {
        return modelService.updateModel(modelId, name, key, description);
    }

    @Operation(
            summary = "Delete a model",
            description = "Delete a specific model by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Model deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Model not found")
    })
    @DeleteMapping("/{modelId}")
    @CacheEvict(value = {"models", "model"}, key = "#modelId", allEntries = true)
    public ResponseEntity<Void> deleteModel(
            @Parameter(description = "Model ID", example = "model-123")
            @PathVariable String modelId) {
        modelService.deleteModel(modelId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get model editor source",
            description = "Retrieve the editor source content of a specific model"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Model source retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Model not found")
    })
    @GetMapping("/{modelId}/source")
    public ResponseEntity<byte[]> getModelSource(
            @Parameter(description = "Model ID", example = "model-123")
            @PathVariable String modelId) {
        byte[] source = modelService.getModelEditorSource(modelId);
        return ResponseEntity.ok(source);
    }

    @Operation(
            summary = "Update model editor source",
            description = "Update the editor source content of a specific model"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Model source updated successfully"),
            @ApiResponse(responseCode = "404", description = "Model not found"),
            @ApiResponse(responseCode = "400", description = "Invalid source content")
    })
    @PostMapping("/{modelId}/source")
    @CacheEvict(value = {"models", "model"}, key = "#modelId", allEntries = true)
    public ResponseEntity<Void> saveModelSource(
            @Parameter(description = "Model ID", example = "model-123")
            @PathVariable String modelId,
            @Parameter(description = "Source content as bytes")
            @RequestBody byte[] source) {
        modelService.saveModelEditorSource(modelId, source);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get model statistics",
            description = "Retrieve statistical information about all models"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Model statistics retrieved successfully")
    })
    @GetMapping("/statistics")
    @Cacheable("modelStatistics")
    public Map<String, Object> getModelStatistics() {
        return modelService.getModelStatistics();
    }
}
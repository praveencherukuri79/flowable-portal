package com.example.backend.controller;

import com.example.backend.dto.DeploymentDto;
import com.example.backend.service.FlowableDeploymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Controller for Flowable Deployment operations.
 * Provides deployment management APIs with verbose DTOs.
 */
@RestController
@RequestMapping("/api/flowable/deployment")
@Tag(name = "Deployment Management", description = "APIs for managing Flowable process deployments and resources")
public class FlowableDeploymentController {

    @Autowired
    private FlowableDeploymentService deploymentService;

    /**
     * Get all deployments.
     * @return list of DeploymentDto
     */
    @Operation(
        summary = "Get All Deployments",
        description = "Retrieves all process deployments with comprehensive metadata"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved deployments",
            content = @Content(schema = @Schema(implementation = DeploymentDto.class))
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public List<DeploymentDto> getAllDeployments() {
        return deploymentService.getAllDeployments();
    }

    /**
     * Get a specific deployment.
     * @param deploymentId deployment id
     * @return DeploymentDto
     */
    @GetMapping("/{deploymentId}")
    public DeploymentDto getDeployment(@PathVariable String deploymentId) {
        return deploymentService.getDeployment(deploymentId);
    }

    /**
     * Deploy a process from file upload.
     * @param file BPMN file
     * @param name deployment name
     * @return DeploymentDto
     */
    @PostMapping("/upload")
    public DeploymentDto deployProcess(@RequestParam("file") MultipartFile file,
                                     @RequestParam(required = false) String name) {
        return deploymentService.deployProcess(file, name);
    }

    /**
     * Deploy a process from classpath resource.
     * @param resourcePath classpath resource path
     * @param name deployment name
     * @return DeploymentDto
     */
    @PostMapping("/classpath")
    public DeploymentDto deployProcessFromClasspath(@RequestParam String resourcePath,
                                                  @RequestParam String name) {
        return deploymentService.deployProcessFromClasspath(resourcePath, name);
    }

    /**
     * Delete a deployment.
     * @param deploymentId deployment id
     * @param cascade cascade deletion
     * @return status message
     */
    @DeleteMapping("/{deploymentId}")
    public Map<String, String> deleteDeployment(@PathVariable String deploymentId,
                                              @RequestParam(defaultValue = "false") boolean cascade) {
        deploymentService.deleteDeployment(deploymentId, cascade);
        return Map.of("status", "deleted");
    }

    /**
     * Get resource names for a deployment.
     * @param deploymentId deployment id
     * @return list of resource names
     */
    @GetMapping("/{deploymentId}/resources")
    public List<String> getResourceNames(@PathVariable String deploymentId) {
        return deploymentService.getResourceNames(deploymentId);
    }

    /**
     * Download a specific resource.
     * @param deploymentId deployment id
     * @param resourceName resource name
     * @return resource bytes
     */
    @GetMapping("/{deploymentId}/resources/{resourceName}")
    public ResponseEntity<byte[]> getResource(@PathVariable String deploymentId,
                                            @PathVariable String resourceName) {
        byte[] resource = deploymentService.getResource(deploymentId, resourceName);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + resourceName + "\"")
                .body(resource);
    }

    /**
     * Get deployment statistics.
     * @return statistics map
     */
    @GetMapping("/statistics")
    public Map<String, Object> getDeploymentStatistics() {
        return deploymentService.getDeploymentStatistics();
    }
}
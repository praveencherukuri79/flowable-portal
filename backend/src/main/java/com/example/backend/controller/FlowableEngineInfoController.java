package com.example.backend.controller;

import com.example.backend.dto.EngineInfoDto;
import com.example.backend.service.FlowableEngineInfoService;
import com.example.backend.util.ErrorHandlingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for Flowable Engine Information.
 * Provides engine monitoring and system information APIs with verbose DTOs.
 */
@RestController
@RequestMapping("/api/flowable/engine")
@Tag(name = "Engine Information", description = "APIs for monitoring Flowable engine status and system information")
public class FlowableEngineInfoController {

    private static final Logger logger = LoggerFactory.getLogger(FlowableEngineInfoController.class);

    @Autowired
    private FlowableEngineInfoService engineInfoService;

    /**
     * Get engine information.
     * @return EngineInfoDto
     */
    @Operation(
        summary = "Get Engine Information",
        description = "Retrieves comprehensive Flowable engine information including version, database, and configuration details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved engine information",
            content = @Content(schema = @Schema(implementation = EngineInfoDto.class))
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getEngineInfo() {
        return ErrorHandlingUtils.executeWithErrorHandling(
            () -> {
                logger.info("Retrieving Flowable engine information");
                return engineInfoService.getEngineInfo();
            },
            "Engine information retrieved successfully",
            "Failed to retrieve engine information",
            logger
        );
    }

    @Operation(
            summary = "Get engine properties",
            description = "Retrieve configuration properties of the Flowable engine"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Engine properties retrieved successfully")
    })
    @GetMapping("/properties")
    @Cacheable("engineProperties")
    public ResponseEntity<Map<String, Object>> getEngineProperties() {
        return ErrorHandlingUtils.executeWithErrorHandling(
            () -> {
                logger.debug("Retrieving engine properties");
                return engineInfoService.getEngineProperties();
            },
            "Engine properties retrieved successfully",
            "Failed to retrieve engine properties",
            logger
        );
    }

    @Operation(
            summary = "Get database table counts",
            description = "Retrieve row counts for all Flowable database tables"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Table counts retrieved successfully")
    })
    @GetMapping("/tables")
    @Cacheable("tableCounts")
    public ResponseEntity<Map<String, Object>> getTableCounts() {
        return ErrorHandlingUtils.executeWithErrorHandling(
            () -> {
                logger.debug("Retrieving table counts");
                return engineInfoService.getTableCounts();
            },
            "Table counts retrieved successfully",
            "Failed to retrieve table counts",
            logger
        );
    }

    @Operation(
            summary = "Get job statistics",
            description = "Retrieve statistical information about Flowable jobs"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job statistics retrieved successfully")
    })
    @GetMapping("/jobs/statistics")
    @Cacheable("jobStatistics")
    public ResponseEntity<Map<String, Object>> getJobStatistics() {
        return ErrorHandlingUtils.executeWithErrorHandling(
            () -> {
                logger.debug("Retrieving job statistics");
                return engineInfoService.getJobStatistics();
            },
            "Job statistics retrieved successfully",
            "Failed to retrieve job statistics",
            logger
        );
    }

    @Operation(
            summary = "Get system health check",
            description = "Perform a comprehensive health check of the Flowable system"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Health check completed successfully"),
            @ApiResponse(responseCode = "503", description = "System unhealthy")
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        return ErrorHandlingUtils.executeHealthCheck(
            () -> {
                logger.info("Performing system health check");
                return engineInfoService.getSystemHealth();
            },
            "System health check completed",
            "Health check failed",
            logger
        );
    }
}
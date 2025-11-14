package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.ProcessStartResponse;
import com.example.backend.service.ProcessManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * Clean controller for Flowable Runtime operations.
 * Business logic delegated to ProcessManagementService.
 */
@RestController
@RequestMapping("/api/flowable/runtime")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Flowable Runtime", description = "Generic Flowable runtime APIs for process management")
@SecurityRequirement(name = "Bearer Authentication")
public class FlowableRuntimeController {
    
    private final ProcessManagementService processManagementService;

    @PostMapping("/start/{processKey}")
    @Operation(summary = "Start process", description = "Start a new process instance with optional variables and business key")
    @PreAuthorize("hasAnyRole('MAKER', 'ADMIN')")
    public ResponseEntity<ProcessStartResponse> startProcess(
            @PathVariable String processKey,
            @RequestBody(required = false) Map<String, Object> payload,
            Principal principal
    ) {
        log.info(">>> Received start process request for: {}", processKey);
        log.info(">>> User: {}", principal != null ? principal.getName() : "anonymous");
        log.info(">>> Payload: {}", payload);
        
        String initiator = principal != null ? principal.getName() : null;
        ProcessStartResponse response = processManagementService.startProcess(processKey, payload, initiator);
        
        log.info(">>> Returning response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/suspend/{processInstanceId}")
    @Operation(summary = "Suspend process", description = "Suspend a running process instance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> suspendProcessInstance(@PathVariable String processInstanceId) {
        processManagementService.suspendProcess(processInstanceId);
        return ResponseEntity.ok(ApiResponse.success("Process instance suspended", 
                Map.of("processInstanceId", processInstanceId)));
    }

    @PostMapping("/activate/{processInstanceId}")
    @Operation(summary = "Activate process", description = "Activate a suspended process instance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> activateProcessInstance(@PathVariable String processInstanceId) {
        processManagementService.activateProcess(processInstanceId);
        return ResponseEntity.ok(ApiResponse.success("Process instance activated", 
                Map.of("processInstanceId", processInstanceId)));
    }
    
    @DeleteMapping("/{processInstanceId}")
    @Operation(summary = "Delete process", description = "Delete a process instance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteProcessInstance(
            @PathVariable String processInstanceId,
            @RequestParam(required = false, defaultValue = "Deleted by admin") String reason
    ) {
        processManagementService.deleteProcess(processInstanceId, reason);
        return ResponseEntity.ok(ApiResponse.success("Process instance deleted", 
                Map.of("processInstanceId", processInstanceId, "reason", reason)));
    }
    
    @GetMapping("/{processInstanceId}/variables")
    @Operation(summary = "Get process variables", description = "Get all variables for a process instance")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getProcessVariables(@PathVariable String processInstanceId) {
        Map<String, Object> variables = processManagementService.getProcessVariables(processInstanceId);
        return ResponseEntity.ok(variables);
    }
    
    @PutMapping("/{processInstanceId}/variables/{variableName}")
    @Operation(summary = "Set process variable", description = "Set a variable on a process instance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> setProcessVariable(
            @PathVariable String processInstanceId,
            @PathVariable String variableName,
            @RequestBody Object value
    ) {
        processManagementService.setProcessVariable(processInstanceId, variableName, value);
        return ResponseEntity.ok(ApiResponse.success("Variable set successfully", 
                Map.of("variableName", variableName, "value", value)));
    }
}

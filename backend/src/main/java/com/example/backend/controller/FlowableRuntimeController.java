package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.ProcessStartResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/flowable/runtime")
@RequiredArgsConstructor
@Tag(name = "Flowable Runtime", description = "Generic Flowable runtime APIs for process management")
@SecurityRequirement(name = "Bearer Authentication")
public class FlowableRuntimeController {
    
    private final RuntimeService runtimeService;

    @PostMapping("/start/{processKey}")
    @Operation(summary = "Start process", description = "Start a new process instance with optional variables and business key")
    @PreAuthorize("hasAnyRole('MAKER', 'ADMIN')")
    public ResponseEntity<ProcessStartResponse> startProcess(
            @PathVariable String processKey,
            @RequestBody(required = false) Map<String, Object> payload,
            Principal principal
    ) {
        Map<String, Object> variables = payload != null ? new HashMap<>(payload) : new HashMap<>();
        
        // Add initiator automatically
        if (principal != null) {
            variables.putIfAbsent("initiator", principal.getName());
            variables.putIfAbsent("startedBy", principal.getName());
        }
        
        // Generate sheetId for all processes if not provided
        if (!variables.containsKey("sheetId")) {
            String sheetId = "SHEET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            variables.put("sheetId", sheetId);
        }
        
        // Extract or generate business key (use sheetId as business key if not provided)
        String businessKey = variables.containsKey("businessKey") 
            ? variables.get("businessKey").toString() 
            : variables.get("sheetId").toString();
        
        // Start process
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                processKey,
                businessKey,
                variables
        );
        
        ProcessStartResponse response = ProcessStartResponse.builder()
                .processInstanceId(processInstance.getId())
                .processDefinitionId(processInstance.getProcessDefinitionId())
                .processKey(processKey)
                .businessKey(processInstance.getBusinessKey())
                .message("Process started successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/suspend/{processInstanceId}")
    @Operation(summary = "Suspend process", description = "Suspend a running process instance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> suspendProcessInstance(@PathVariable String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
        return ResponseEntity.ok(ApiResponse.success("Process instance suspended", 
                Map.of("processInstanceId", processInstanceId)));
    }

    @PostMapping("/activate/{processInstanceId}")
    @Operation(summary = "Activate process", description = "Activate a suspended process instance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> activateProcessInstance(@PathVariable String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
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
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        return ResponseEntity.ok(ApiResponse.success("Process instance deleted", 
                Map.of("processInstanceId", processInstanceId, "reason", reason)));
    }
    
    @GetMapping("/{processInstanceId}/variables")
    @Operation(summary = "Get process variables", description = "Get all variables for a process instance")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getProcessVariables(@PathVariable String processInstanceId) {
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
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
        runtimeService.setVariable(processInstanceId, variableName, value);
        return ResponseEntity.ok(ApiResponse.success("Variable set successfully", 
                Map.of("variableName", variableName, "value", value)));
    }
}

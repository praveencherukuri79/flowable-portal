package com.example.backend.controller;

import com.example.backend.dto.ProcessInstanceDto;
import com.example.backend.dto.TaskDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/process")
@RequiredArgsConstructor
@Tag(name = "Process Control", description = "Generic process control APIs for admin")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class ProcessControlController {
    
    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final TaskService taskService;
    
    @PostMapping("/start/{processKey}")
    @Operation(summary = "Start process by key", description = "Start a new process instance from any deployed definition")
    public ResponseEntity<Map<String, Object>> startProcess(
            @PathVariable String processKey,
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        String businessKey = payload != null ? (String) payload.get("businessKey") : null;
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = payload != null ? (Map<String, Object>) payload.get("variables") : new HashMap<>();
        
        if (variables == null) {
            variables = new HashMap<>();
        }
        
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                processKey,
                businessKey,
                variables
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceId", processInstance.getId());
        response.put("processDefinitionId", processInstance.getProcessDefinitionId());
        response.put("businessKey", processInstance.getBusinessKey());
        response.put("message", "Process started successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/instances/running")
    @Operation(summary = "Get all running instances", description = "Retrieve all currently running process instances")
    public ResponseEntity<List<ProcessInstanceDto>> getRunningInstances() {
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .active()
                .orderByProcessInstanceId()
                .desc()
                .list();
        
        List<ProcessInstanceDto> dtos = instances.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/instances/{processInstanceId}")
    @Operation(summary = "Get instance details", description = "Get detailed information about a process instance")
    public ResponseEntity<ProcessInstanceDto> getInstanceDetails(@PathVariable String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        
        if (instance == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(convertToDto(instance));
    }
    
    @GetMapping("/instances/{processInstanceId}/variables")
    @Operation(summary = "Get instance variables", description = "Get all variables for a process instance")
    public ResponseEntity<Map<String, Object>> getInstanceVariables(@PathVariable String processInstanceId) {
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        return ResponseEntity.ok(variables);
    }
    
    @PutMapping("/instances/{processInstanceId}/variables")
    @Operation(summary = "Update instance variables", description = "Update variables for a process instance")
    public ResponseEntity<Map<String, String>> updateInstanceVariables(
            @PathVariable String processInstanceId,
            @RequestBody Map<String, Object> variables
    ) {
        runtimeService.setVariables(processInstanceId, variables);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Variables updated successfully");
        response.put("processInstanceId", processInstanceId);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/instances/{processInstanceId}")
    @Operation(summary = "Delete process instance", description = "Forcefully delete a process instance")
    public ResponseEntity<Map<String, String>> deleteInstance(
            @PathVariable String processInstanceId,
            @RequestParam(required = false) String deleteReason
    ) {
        runtimeService.deleteProcessInstance(
                processInstanceId,
                deleteReason != null ? deleteReason : "Deleted by admin"
        );
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Process instance deleted successfully");
        response.put("processInstanceId", processInstanceId);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/instances/{processInstanceId}/suspend")
    @Operation(summary = "Suspend process instance", description = "Suspend a running process instance")
    public ResponseEntity<Map<String, String>> suspendInstance(@PathVariable String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Process instance suspended");
        response.put("processInstanceId", processInstanceId);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/instances/{processInstanceId}/activate")
    @Operation(summary = "Activate process instance", description = "Activate a suspended process instance")
    public ResponseEntity<Map<String, String>> activateInstance(@PathVariable String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Process instance activated");
        response.put("processInstanceId", processInstanceId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/instances/{processInstanceId}/tasks")
    @Operation(summary = "Get instance tasks", description = "Get all active tasks for a process instance")
    public ResponseEntity<List<TaskDto>> getInstanceTasks(@PathVariable String processInstanceId) {
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();
        
        List<TaskDto> dtos = tasks.stream()
                .map(this::convertTaskToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    private ProcessInstanceDto convertToDto(ProcessInstance pi) {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(pi.getProcessDefinitionId())
                .singleResult();
        
        ProcessInstanceDto dto = new ProcessInstanceDto();
        dto.id = pi.getId();
        dto.processDefinitionId = pi.getProcessDefinitionId();
        dto.processDefinitionKey = pi.getProcessDefinitionKey();
        dto.name = pd != null ? pd.getName() : pi.getProcessDefinitionName();
        dto.businessKey = pi.getBusinessKey();
        dto.status = pi.isSuspended() ? "SUSPENDED" : "ACTIVE";
        dto.startTime = pi.getStartTime() != null ? pi.getStartTime().toString() : null;
        dto.startUserId = pi.getStartUserId();
        dto.suspended = pi.isSuspended();
        
        return dto;
    }
    
    private TaskDto convertTaskToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.id = task.getId();
        dto.name = task.getName();
        dto.description = task.getDescription();
        dto.assignee = task.getAssignee();
        dto.owner = task.getOwner();
        dto.processInstanceId = task.getProcessInstanceId();
        dto.processDefinitionId = task.getProcessDefinitionId();
        dto.taskDefinitionKey = task.getTaskDefinitionKey();
        dto.createTime = task.getCreateTime();
        dto.dueDate = task.getDueDate();
        dto.priority = task.getPriority();
        dto.suspended = task.isSuspended() ? "true" : "false";
        
        return dto;
    }
}


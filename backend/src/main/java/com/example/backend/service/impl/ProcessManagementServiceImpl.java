package com.example.backend.service.impl;

import com.example.backend.dto.ProcessStartResponse;
import com.example.backend.service.ProcessManagementService;
import com.example.backend.util.ProcessVariableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of ProcessManagementService.
 * Handles all business logic for process management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProcessManagementServiceImpl implements ProcessManagementService {

    private final RuntimeService runtimeService;

    @Override
    @CacheEvict(value = "processInstances", allEntries = true)
    public ProcessStartResponse startProcess(String processKey, Map<String, Object> variables, String initiator) {
        log.info("=== PROCESS START REQUEST ===");
        log.info("Process Key: {}", processKey);
        log.info("Initiator: {}", initiator);
        log.info("Input Variables: {}", variables);

        // Enrich variables
        Map<String, Object> enrichedVariables = variables != null ? new HashMap<>(variables) : new HashMap<>();
        
        // Add initiator
        if (initiator != null) {
            enrichedVariables = ProcessVariableUtils.enrichWithInitiator(enrichedVariables, initiator);
            log.info("Added initiator variables");
        }
        
        // NOTE: sheetId is NO LONGER generated at process level.
        // Each maker task creates its own sheet and stores it as "formKey-sheetId"
        
        // Extract business key (use process instance ID if not provided)
        String businessKey = ProcessVariableUtils.extractBusinessKey(enrichedVariables);
        log.info("Business Key: {}", businessKey);
        
        log.info("Final Variables to be passed: {}", enrichedVariables);

        // Start process
        log.info("Starting process instance...");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                processKey,
                businessKey,
                enrichedVariables
        );

        log.info("✓ Process started successfully!");
        log.info("  Process Instance ID: {}", processInstance.getId());
        log.info("  Process Definition ID: {}", processInstance.getProcessDefinitionId());
        log.info("  Business Key: {}", processInstance.getBusinessKey());
        log.info("=== PROCESS START COMPLETE ===");

        // Build response
        return ProcessStartResponse.builder()
                .processInstanceId(processInstance.getId())
                .processDefinitionId(processInstance.getProcessDefinitionId())
                .processKey(processKey)
                .businessKey(processInstance.getBusinessKey())
                .message("Process started successfully")
                .build();
    }

    @Override
    @CacheEvict(value = "processInstances", allEntries = true)
    public void suspendProcess(String processInstanceId) {
        log.info("Suspending process instance: {}", processInstanceId);
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    @Override
    @CacheEvict(value = "processInstances", allEntries = true)
    public void activateProcess(String processInstanceId) {
        log.info("Activating process instance: {}", processInstanceId);
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    @Override
    @CacheEvict(value = "processInstances", allEntries = true)
    public void deleteProcess(String processInstanceId, String reason) {
        log.info("Deleting process instance: {} with reason: {}", processInstanceId, reason);
        runtimeService.deleteProcessInstance(processInstanceId, reason);
    }

    @Override
    @Cacheable(value = "processVariables", key = "#processInstanceId")
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    @Override
    @CacheEvict(value = "processVariables", key = "#processInstanceId")
    public void setProcessVariable(String processInstanceId, String variableName, Object value) {
        log.info("Setting variable {} on process {}", variableName, processInstanceId);
        runtimeService.setVariable(processInstanceId, variableName, value);
    }

    @Override
    @CacheEvict(value = "processVariables", key = "#processInstanceId")
    public void setProcessVariables(String processInstanceId, Map<String, Object> variables) {
        log.info("Setting {} variables on process {}", variables.size(), processInstanceId);
        runtimeService.setVariables(processInstanceId, variables);
    }

    @Override
    @Cacheable(value = "processInstances", key = "#processInstanceId")
    public ProcessInstance getProcessInstance(String processInstanceId) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }
}


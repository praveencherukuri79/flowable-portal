package com.example.backend.service;

import com.example.backend.dto.ProcessStartResponse;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.Map;

/**
 * Service for managing process instances.
 * Consolidates business logic for process operations.
 */
public interface ProcessManagementService {

    /**
     * Start a process with variables and initiator context
     */
    ProcessStartResponse startProcess(String processKey, Map<String, Object> variables, String initiator);

    /**
     * Suspend a process instance
     */
    void suspendProcess(String processInstanceId);

    /**
     * Activate a suspended process instance
     */
    void activateProcess(String processInstanceId);

    /**
     * Delete a process instance
     */
    void deleteProcess(String processInstanceId, String reason);

    /**
     * Get process variables
     */
    Map<String, Object> getProcessVariables(String processInstanceId);

    /**
     * Set a process variable
     */
    void setProcessVariable(String processInstanceId, String variableName, Object value);

    /**
     * Set multiple process variables
     */
    void setProcessVariables(String processInstanceId, Map<String, Object> variables);

    /**
     * Get process instance details
     */
    ProcessInstance getProcessInstance(String processInstanceId);
}


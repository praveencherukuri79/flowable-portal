package com.example.backend.service;

import java.util.Map;

/**
 * Service interface for Flowable runtime operations.
 */
public interface FlowableRuntimeService {
    
    /**
     * Start a process instance by process key.
     * @param processKey process definition key
     * @return process instance id
     */
    String startProcess(String processKey);
    
    /**
     * Start a process instance with variables.
     * @param processKey process definition key
     * @param variables process variables
     * @return process instance id
     */
    String startProcessWithVariables(String processKey, Map<String, Object> variables);
    
    /**
     * Suspend a process instance.
     * @param processInstanceId process instance id
     */
    void suspendProcessInstance(String processInstanceId);
    
    /**
     * Activate a suspended process instance.
     * @param processInstanceId process instance id
     */
    void activateProcessInstance(String processInstanceId);
    
    /**
     * Delete a process instance.
     * @param processInstanceId process instance id
     * @param deleteReason reason for deletion
     */
    void deleteProcessInstance(String processInstanceId, String deleteReason);
    
    /**
     * Set process variable.
     * @param processInstanceId process instance id
     * @param variableName variable name
     * @param value variable value
     */
    void setVariable(String processInstanceId, String variableName, Object value);
    
    /**
     * Get process variable.
     * @param processInstanceId process instance id
     * @param variableName variable name
     * @return variable value
     */
    Object getVariable(String processInstanceId, String variableName);
}

package com.example.backend.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Database update delegate for Flowable process execution.
 * Handles database operations during process flow execution.
 */
@Component("dbUpdateDelegate")
public class DbUpdateDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(DbUpdateDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("DB Update Delegate executed for processInstanceId: {}", execution.getProcessInstanceId());
        
        // Get process variables
        String processInstanceId = execution.getProcessInstanceId();
        String processDefinitionId = execution.getProcessDefinitionId();
        
        logger.info("Processing database update for process: {} (instance: {})", processDefinitionId, processInstanceId);
        
        // Add database update logic here
        // For example: update retention offer status, log audit trail, etc.
        
        // Set completion flag
        execution.setVariable("dbUpdateCompleted", true);
        execution.setVariable("dbUpdateTimestamp", System.currentTimeMillis());
    }
}
package com.example.backend.flowable;

import com.example.backend.service.DataMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component("dataMigrationTaskListener")
@RequiredArgsConstructor
@Slf4j
public class DataMigrationTaskListener implements TaskListener {
    
    private final DataMigrationService dataMigrationService;
    
    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("=== Data Migration Task Listener Triggered ===");
        log.info("Task ID: {}", delegateTask.getId());
        log.info("Task Name: {}", delegateTask.getName());
        
        try {
            // Get processInstanceId - this is always available in the task
            String processInstanceId = delegateTask.getProcessInstanceId();
            
            if (processInstanceId == null || processInstanceId.isEmpty()) {
                log.error("ProcessInstanceId not found");
                throw new RuntimeException("ProcessInstanceId is required for data migration");
            }
            
            log.info("Starting data migration for processInstanceId: {}", processInstanceId);
            
            // Migrate all staging data to actual tables
            // The service will look up all 3 sheets (product, plan, item) based on processInstanceId
            dataMigrationService.migrateAllStagingToActual(processInstanceId);
            
            log.info("=== Data Migration Completed Successfully ===");
            
        } catch (Exception e) {
            log.error("Error during data migration", e);
            throw new RuntimeException("Data migration failed: " + e.getMessage(), e);
        }
    }
}


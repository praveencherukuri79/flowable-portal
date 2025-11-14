package com.example.backend.flowable;

import com.example.backend.dto.PlanDto;
import com.example.backend.service.PlanService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Task listener that saves plans to database when maker completes editing (Stage 2)
 * This separates business logic from process API
 */
@Component("planTaskListener")
@Slf4j
public class PlanTaskListener implements TaskListener {
    
    @Autowired
    private PlanService planService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            log.info("PlanTaskListener triggered for task: {}", delegateTask.getName());
            
            // Get plans from task variables
            Object plansObj = delegateTask.getVariable("plans");
            String sheetId = (String) delegateTask.getVariable("sheetId");
            String editedBy = delegateTask.getAssignee();
            
            if (plansObj != null && sheetId != null) {
                // Convert plans to DTOs
                List<PlanDto> plans;
                if (plansObj instanceof String) {
                    plans = objectMapper.readValue((String) plansObj, new TypeReference<List<PlanDto>>(){});
                } else if (plansObj instanceof List) {
                    plans = objectMapper.convertValue(plansObj, new TypeReference<List<PlanDto>>(){});
                } else {
                    log.warn("Unknown plans type: {}", plansObj.getClass());
                    return;
                }
                
                // Save plans using service (business logic separated)
                planService.savePlansFromTask(sheetId, plans, editedBy);
                
                log.info("Saved {} plans for sheet {}", plans.size(), sheetId);
            } else {
                log.warn("Missing required variables: sheetId={}, plans={}", 
                        sheetId, plansObj != null);
            }
            
        } catch (Exception e) {
            log.error("Error in PlanTaskListener", e);
            throw new RuntimeException("Failed to save plans: " + e.getMessage(), e);
        }
    }
}


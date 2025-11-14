package com.example.backend.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Validation delegate for retention offer approval process.
 * Validates offer data and sets approval status.
 */
@Component("validationDelegate")
public class ValidationDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ValidationDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("Validation Delegate executed for processInstanceId: {}", execution.getProcessInstanceId());
        
        try {
            // Get retention offer data from process variables
            @SuppressWarnings("unchecked")
            Map<String, Object> offerData = (Map<String, Object>) execution.getVariable("retentionOfferData");
            
            boolean isValid = validateOfferData(offerData);
            
            // Set validation results
            execution.setVariable("validationPassed", isValid);
            execution.setVariable("validationTimestamp", LocalDateTime.now().toString());
            
            if (!isValid) {
                execution.setVariable("validationError", "Retention offer data validation failed");
                logger.warn("Validation failed for process instance: {}", execution.getProcessInstanceId());
            } else {
                logger.info("Validation passed for process instance: {}", execution.getProcessInstanceId());
            }
            
        } catch (Exception e) {
            logger.error("Error during validation for process instance: {}", execution.getProcessInstanceId(), e);
            execution.setVariable("validationPassed", false);
            execution.setVariable("validationError", "Validation error: " + e.getMessage());
        }
    }
    
    private boolean validateOfferData(Map<String, Object> offerData) {
        if (offerData == null) {
            logger.warn("Offer data is null");
            return false;
        }
        
        // Validate required fields
        String customerId = (String) offerData.get("customerId");
        Object rateObj = offerData.get("rate");
        Object apyObj = offerData.get("apy");
        String effectiveDate = (String) offerData.get("effectiveDate");
        
        if (customerId == null || customerId.trim().isEmpty()) {
            logger.warn("Customer ID is missing or empty");
            return false;
        }
        
        if (rateObj == null) {
            logger.warn("Rate is missing");
            return false;
        }
        
        if (apyObj == null) {
            logger.warn("APY is missing");
            return false;
        }
        
        if (effectiveDate == null || effectiveDate.trim().isEmpty()) {
            logger.warn("Effective date is missing or empty");
            return false;
        }
        
        // Additional business validation logic can be added here
        
        return true;
    }
}
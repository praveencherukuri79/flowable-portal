package com.example.backend.flowable;

import com.example.backend.dto.ProductDto;
import com.example.backend.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Task listener that saves products to database when maker completes editing (Stage 1)
 * This separates business logic from process API
 */
@Component("productTaskListener")
@Slf4j
public class ProductTaskListener implements TaskListener {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            log.info("=== ProductTaskListener START ===");
            log.info("Task Name: {}", delegateTask.getName());
            log.info("Task ID: {}", delegateTask.getId());
            log.info("Process Instance ID: {}", delegateTask.getProcessInstanceId());
            log.info("Assignee: {}", delegateTask.getAssignee());
            
            // Get products from task variables
            Object productsObj = delegateTask.getVariable("products");
            String sheetId = (String) delegateTask.getVariable("sheetId");
            String editedBy = delegateTask.getAssignee();
            
            log.info("Retrieved variables - sheetId: {}, products: {}", 
                    sheetId, productsObj != null ? "present" : "null");
            
            if (productsObj != null && sheetId != null) {
                // Convert products to DTOs
                List<ProductDto> products;
                if (productsObj instanceof String) {
                    log.info("Products is String, parsing JSON");
                    products = objectMapper.readValue((String) productsObj, new TypeReference<List<ProductDto>>(){});
                } else if (productsObj instanceof List) {
                    log.info("Products is List, converting");
                    products = objectMapper.convertValue(productsObj, new TypeReference<List<ProductDto>>(){});
                } else {
                    log.warn("Unknown products type: {}", productsObj.getClass());
                    return;
                }
                
                log.info("Parsed {} products", products.size());
                
                // Save products using service (business logic separated)
                productService.saveProductsFromTask(sheetId, products, editedBy);
                
                log.info("✓ Successfully saved {} products for sheet {}", products.size(), sheetId);
            } else {
                log.error("✗ Missing required variables - sheetId: {}, products: {}", 
                        sheetId != null ? sheetId : "NULL", 
                        productsObj != null ? "present" : "NULL");
            }
            
            log.info("=== ProductTaskListener END ===");
            
        } catch (Exception e) {
            log.error("✗ Error in ProductTaskListener", e);
            throw new RuntimeException("Failed to save products: " + e.getMessage(), e);
        }
    }
}


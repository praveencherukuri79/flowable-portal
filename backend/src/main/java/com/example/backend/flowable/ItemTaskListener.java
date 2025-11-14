package com.example.backend.flowable;

import com.example.backend.dto.ItemDto;
import com.example.backend.service.ItemService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Task listener that saves items to database when maker completes editing (Stage 3)
 * This separates business logic from process API
 */
@Component("itemTaskListener")
@Slf4j
public class ItemTaskListener implements TaskListener {
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            log.info("ItemTaskListener triggered for task: {}", delegateTask.getName());
            
            // Get items from task variables
            Object itemsObj = delegateTask.getVariable("items");
            String sheetId = (String) delegateTask.getVariable("sheetId");
            String editedBy = delegateTask.getAssignee();
            
            if (itemsObj != null && sheetId != null) {
                // Convert items to DTOs
                List<ItemDto> items;
                if (itemsObj instanceof String) {
                    items = objectMapper.readValue((String) itemsObj, new TypeReference<List<ItemDto>>(){});
                } else if (itemsObj instanceof List) {
                    items = objectMapper.convertValue(itemsObj, new TypeReference<List<ItemDto>>(){});
                } else {
                    log.warn("Unknown items type: {}", itemsObj.getClass());
                    return;
                }
                
                // Save items using service (business logic separated)
                itemService.saveItemsFromTask(sheetId, items, editedBy);
                
                log.info("Saved {} items for sheet {}", items.size(), sheetId);
            } else {
                log.warn("Missing required variables: sheetId={}, items={}", 
                        sheetId, itemsObj != null);
            }
            
        } catch (Exception e) {
            log.error("Error in ItemTaskListener", e);
            throw new RuntimeException("Failed to save items: " + e.getMessage(), e);
        }
    }
}


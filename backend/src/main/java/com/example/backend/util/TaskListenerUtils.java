package com.example.backend.util;

import com.example.backend.dto.ItemStagingDto;
import com.example.backend.dto.PlanStagingDto;
import com.example.backend.dto.ProductStagingDto;
import com.example.backend.dto.SheetDto;
import com.example.backend.service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Generic utility for TaskListener operations
 * Eliminates code duplication across Item/Plan/Product TaskListeners
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskListenerUtils {
    
    private final ObjectMapper objectMapper;
    private final SheetService sheetService;
    private final ItemStagingService itemStagingService;
    private final PlanStagingService planStagingService;
    private final ProductStagingService productStagingService;
    
    /**
     * Generic method to handle Item staging logic
     */
    public <T> void processItemStaging(
            DelegateTask delegateTask,
            String entityType,
            TypeReference<List<T>> typeReference,
            BiPredicate<T, T> hasChangedPredicate,
            Function<T, String> getNameFunction) {
        
        try {
            log.info("=== Processing {} TaskListener ===", entityType);
            
            // Extract task info
            String processInstanceId = delegateTask.getProcessInstanceId();
            String taskDefinitionKey = delegateTask.getTaskDefinitionKey();
            String formKey = delegateTask.getFormKey(); // THIS IS THE ACTUAL FORM KEY (e.g., /maker/item-edit)
            String editedBy = delegateTask.getAssignee();
            Object dataObj = delegateTask.getVariable(entityType.toLowerCase() + "s");
            
            log.info("Task Definition Key: {}", taskDefinitionKey);
            log.info("Form Key: {}", formKey);
            log.info("Process Instance ID: {}", processInstanceId);
            
            // Validate this is a maker task - TaskListener should only process maker submissions
            // Checker tasks don't have these TaskListeners, but defensive check for formKey
            if (formKey == null || !formKey.contains("/maker/")) {
                log.info("⚠ Not a maker task (formKey: {}). Skipping TaskListener.", formKey);
                return;
            }
            
            // Check the reason for task completion - only process if reason is "submit"
            Object reasonObj = delegateTask.getVariable("reason");
            String reason = reasonObj != null ? reasonObj.toString() : null;
            log.info("Task completion reason: {}", reason);
            
            if (!"submit".equalsIgnoreCase(reason)) {
                log.info("⚠ Task completion reason is not 'submit' (reason: {}). Skipping data save.", reason);
                return;
            }
            
            // Validate data exists - if no data, skip processing
            if (dataObj == null) {
                log.info("⚠ No {} data found in process variables. Skipping TaskListener.", entityType);
                return;
            }
            
            // Validate required fields
            ValidationUtils.requireNonEmpty(processInstanceId, "ProcessInstanceId is required");
            
            if (editedBy == null || editedBy.isEmpty()) {
                log.warn("⚠ Task assignee is null, using 'system' as default");
                editedBy = "system";
            }
            
            String variableName = formKey + "-sheetId";
            
            // Parse incoming data
            List<T> incomingData = parseData(dataObj, typeReference);
            ValidationUtils.requireNonEmpty(incomingData, entityType + "s list cannot be empty");
            
            log.info("Parsed {} {}", incomingData.size(), entityType.toLowerCase() + "s");
            
            // Check if sheet exists using entityType
            String sheetType = entityType.toLowerCase(); // item, plan, or product
            java.util.Optional<SheetDto> existingSheetOpt = sheetService.findSheetByProcessAndType(processInstanceId, sheetType);
            
            if (existingSheetOpt.isPresent()) {
                // Existing sheet found - create new version
                SheetDto existingSheet = existingSheetOpt.get();
                String existingSheetId = existingSheet.getSheetId();
                log.info("✓ Found existing sheet: {} (version {}). Creating new version.", 
                        existingSheetId, existingSheet.getVersion());
                
                // Load existing data for comparison
                List<T> existingData = loadExistingData(existingSheetId, entityType);
                log.info("✓ Loaded {} existing {} for comparison", existingData.size(), entityType.toLowerCase() + "s");
                
                // Create new sheet with incremented version
                SheetDto newSheet = sheetService.createSheet(processInstanceId, sheetType, editedBy);
                String newSheetId = newSheet.getSheetId();
                log.info("✓ Created new sheet: {} (version {})", newSheetId, newSheet.getVersion());
                
                // Process incoming data: compare with existing, set approval status, and set sheetId in one step
                LocalDateTime now = LocalDateTime.now();
                for (T incoming : incomingData) {
                    // Find matching existing item by comparing business data
                    T matchingExisting = findMatching(existingData, incoming, hasChangedPredicate);
                    
                    if (matchingExisting != null && !hasChangedPredicate.test(matchingExisting, incoming)) {
                        // Data unchanged - preserve approval info
                        copyApprovalInfo(matchingExisting, incoming);
                        log.debug("✓ Preserved approval for unchanged {}: {}", entityType.toLowerCase(), getNameFunction.apply(incoming));
                    } else {
                        // Data changed or new - clear approval
                        clearApproval(incoming);
                        if (matchingExisting != null) {
                            log.info("✓ Revoked approval for changed {}: {}", entityType.toLowerCase(), getNameFunction.apply(incoming));
                        } else {
                            log.info("✓ New {} added: {}", entityType.toLowerCase(), getNameFunction.apply(incoming));
                        }
                    }
                    
                    // Set metadata including sheetId
                    setBasicMetadata(incoming, newSheetId, editedBy, now);
                }
                
                // Save new incoming data to new sheetId (old data already preserved in old sheetId)
                saveData(incomingData, entityType);
                log.info("✓ Saved {} new {} for sheet {}", incomingData.size(), entityType.toLowerCase() + "s", newSheetId);
                
                // Update process variable with new sheetId
                delegateTask.setVariable(variableName, newSheetId);
            } else {
                // Create new sheet (first submission)
                log.info("✓ Creating new sheet for process");
                createNewSheet(delegateTask, incomingData, editedBy, sheetType, variableName, entityType);
            }
            
            log.info("=== {} TaskListener END ===", entityType);
            
        } catch (Exception e) {
            log.error("✗ Error in {} TaskListener", entityType, e);
            throw new RuntimeException("Failed to save " + entityType.toLowerCase() + "s: " + e.getMessage(), e);
        }
    }
    
    private <T> List<T> parseData(Object dataObj, TypeReference<List<T>> typeReference) {
        try {
            if (dataObj instanceof String) {
                return objectMapper.readValue((String) dataObj, typeReference);
            } else if (dataObj instanceof List) {
                return objectMapper.convertValue(dataObj, typeReference);
            } else {
                throw new RuntimeException("Invalid data type: " + dataObj.getClass());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse data", e);
        }
    }
    
    private <T> void createNewSheet(
            DelegateTask delegateTask,
            List<T> data,
            String editedBy,
            String sheetType,
            String variableName,
            String entityType) {
        
        String processInstanceId = delegateTask.getProcessInstanceId();
        
        // Create new sheet with sheetType (item, plan, or product)
        SheetDto sheet = sheetService.createSheet(processInstanceId, sheetType, editedBy);
        String sheetId = sheet.getSheetId();
        log.info("✓ Created new sheet: {} for type: {}", sheetId, sheetType);
        
        // Store sheetId in process variables
        delegateTask.setVariable(variableName, sheetId);
        log.info("✓ Stored process variable: {} = {}", variableName, sheetId);
        
        // Set metadata for all items
        LocalDateTime now = LocalDateTime.now();
        for (T item : data) {
            setMetadata(item, sheetId, editedBy, now, false, null, null, "PENDING");
        }
        
        // Save data
        saveData(data, entityType);
        log.info("✓ Saved {} new {}", data.size(), entityType.toLowerCase() + "s");
    }
    
    @SuppressWarnings("unchecked")
    private <T> List<T> loadExistingData(String sheetId, String entityType) {
        switch (entityType) {
            case "Item":
                return (List<T>) itemStagingService.getItemsBySheetId(sheetId);
            case "Plan":
                return (List<T>) planStagingService.getPlansBySheetId(sheetId);
            case "Product":
                return (List<T>) productStagingService.getProductsBySheetId(sheetId);
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }
    
    private <T> void saveData(List<T> data, String entityType) {
        switch (entityType) {
            case "Item":
                itemStagingService.saveItems((List<ItemStagingDto>) data);
                break;
            case "Plan":
                planStagingService.savePlans((List<PlanStagingDto>) data);
                break;
            case "Product":
                productStagingService.saveProducts((List<ProductStagingDto>) data);
                break;
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }
    
    private <T> T findMatching(List<T> existingList, T incoming, BiPredicate<T, T> hasChangedPredicate) {
        for (T existing : existingList) {
            if (!hasChangedPredicate.test(existing, incoming)) {
                return existing;
            }
        }
        return null;
    }
    
    private <T> void setMetadata(T item, String sheetId, String editedBy, LocalDateTime editedAt, 
                                  Boolean approved, String approvedBy, LocalDateTime approvedAt, String status) {
        if (item instanceof ItemStagingDto) {
            ItemStagingDto dto = (ItemStagingDto) item;
            dto.setSheetId(sheetId);
            dto.setEditedBy(editedBy);
            dto.setEditedAt(editedAt);
            dto.setApproved(approved);
            dto.setApprovedBy(approvedBy);
            dto.setApprovedAt(approvedAt);
            dto.setStatus(status);
        } else if (item instanceof PlanStagingDto) {
            PlanStagingDto dto = (PlanStagingDto) item;
            dto.setSheetId(sheetId);
            dto.setEditedBy(editedBy);
            dto.setEditedAt(editedAt);
            dto.setApproved(approved);
            dto.setApprovedBy(approvedBy);
            dto.setApprovedAt(approvedAt);
            dto.setStatus(status);
        } else if (item instanceof ProductStagingDto) {
            ProductStagingDto dto = (ProductStagingDto) item;
            dto.setSheetId(sheetId);
            dto.setEditedBy(editedBy);
            dto.setEditedAt(editedAt);
            dto.setApproved(approved);
            dto.setApprovedBy(approvedBy);
            dto.setApprovedAt(approvedAt);
            dto.setStatus(status);
        }
    }
    
    private <T> void setBasicMetadata(T item, String sheetId, String editedBy, LocalDateTime editedAt) {
        if (item instanceof ItemStagingDto) {
            ItemStagingDto dto = (ItemStagingDto) item;
            dto.setSheetId(sheetId);
            dto.setEditedBy(editedBy);
            dto.setEditedAt(editedAt);
        } else if (item instanceof PlanStagingDto) {
            PlanStagingDto dto = (PlanStagingDto) item;
            dto.setSheetId(sheetId);
            dto.setEditedBy(editedBy);
            dto.setEditedAt(editedAt);
        } else if (item instanceof ProductStagingDto) {
            ProductStagingDto dto = (ProductStagingDto) item;
            dto.setSheetId(sheetId);
            dto.setEditedBy(editedBy);
            dto.setEditedAt(editedAt);
        }
    }
    
    private <T> void copyApprovalInfo(T source, T target) {
        if (source instanceof ItemStagingDto && target instanceof ItemStagingDto) {
            ItemStagingDto src = (ItemStagingDto) source;
            ItemStagingDto tgt = (ItemStagingDto) target;
            tgt.setApproved(src.getApproved());
            tgt.setApprovedBy(src.getApprovedBy());
            tgt.setApprovedAt(src.getApprovedAt());
            tgt.setStatus(src.getStatus());
        } else if (source instanceof PlanStagingDto && target instanceof PlanStagingDto) {
            PlanStagingDto src = (PlanStagingDto) source;
            PlanStagingDto tgt = (PlanStagingDto) target;
            tgt.setApproved(src.getApproved());
            tgt.setApprovedBy(src.getApprovedBy());
            tgt.setApprovedAt(src.getApprovedAt());
            tgt.setStatus(src.getStatus());
        } else if (source instanceof ProductStagingDto && target instanceof ProductStagingDto) {
            ProductStagingDto src = (ProductStagingDto) source;
            ProductStagingDto tgt = (ProductStagingDto) target;
            tgt.setApproved(src.getApproved());
            tgt.setApprovedBy(src.getApprovedBy());
            tgt.setApprovedAt(src.getApprovedAt());
            tgt.setStatus(src.getStatus());
        }
    }
    
    private <T> void clearApproval(T item) {
        if (item instanceof ItemStagingDto) {
            ItemStagingDto dto = (ItemStagingDto) item;
            dto.setApproved(false);
            dto.setApprovedBy(null);
            dto.setApprovedAt(null);
            dto.setStatus("PENDING");
        } else if (item instanceof PlanStagingDto) {
            PlanStagingDto dto = (PlanStagingDto) item;
            dto.setApproved(false);
            dto.setApprovedBy(null);
            dto.setApprovedAt(null);
            dto.setStatus("PENDING");
        } else if (item instanceof ProductStagingDto) {
            ProductStagingDto dto = (ProductStagingDto) item;
            dto.setApproved(false);
            dto.setApprovedBy(null);
            dto.setApprovedAt(null);
            dto.setStatus("PENDING");
        }
    }
}


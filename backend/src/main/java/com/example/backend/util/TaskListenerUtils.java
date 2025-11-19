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
     * Generic method to handle staging logic for Items/Plans/Products
     */
    public <T> void processItemStaging(
            DelegateTask delegateTask,
            String entityType,
            TypeReference<List<T>> typeReference,
            BiPredicate<T, T> hasChangedPredicate,
            Function<T, String> getNameFunction) {
        
        try {
            log.info("=== Processing {} TaskListener ===", entityType);
            
            // Validate and extract task data
            TaskContext context = extractAndValidateContext(delegateTask, entityType);
            if (context == null) {
                return; // Validation failed, already logged
            }
            
            // Parse incoming data
            List<T> incomingData = parseData(context.dataObj, typeReference);
            ValidationUtils.requireNonEmpty(incomingData, entityType + "s list cannot be empty");
            log.info("Parsed {} {}", incomingData.size(), entityType.toLowerCase() + "s");
            
            // Process based on whether sheet exists
            String sheetType = entityType.toLowerCase();
            java.util.Optional<SheetDto> existingSheetOpt = sheetService.findSheetByProcessAndType(
                    context.processInstanceId, sheetType);
            
            if (existingSheetOpt.isPresent()) {
                processResubmission(delegateTask, existingSheetOpt.get(), incomingData, context, 
                        entityType, sheetType, hasChangedPredicate, getNameFunction);
            } else {
                processFirstSubmission(delegateTask, incomingData, context, entityType, sheetType);
            }
            
            log.info("=== {} TaskListener END ===", entityType);
            
        } catch (Exception e) {
            log.error("✗ Error in {} TaskListener", entityType, e);
            throw new RuntimeException("Failed to save " + entityType.toLowerCase() + "s: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract and validate task context
     */
    private TaskContext extractAndValidateContext(DelegateTask delegateTask, String entityType) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        String formKey = delegateTask.getFormKey();
        String editedBy = delegateTask.getAssignee();
        Object dataObj = delegateTask.getVariable(entityType.toLowerCase() + "s");
        Object reasonObj = delegateTask.getVariable("reason");
        String reason = reasonObj != null ? reasonObj.toString() : null;
        
        log.info("Process Instance ID: {}", processInstanceId);
        log.info("Form Key: {}", formKey);
        log.info("Task completion reason: {}", reason);
        
        // Validate maker task
        if (formKey == null || !formKey.contains("/maker/")) {
            log.info("⚠ Not a maker task (formKey: {}). Skipping TaskListener.", formKey);
            return null;
        }
        
        // Validate submit reason
        if (!"submit".equalsIgnoreCase(reason)) {
            log.info("⚠ Task completion reason is not 'submit' (reason: {}). Skipping data save.", reason);
            return null;
        }
        
        // Validate data exists
        if (dataObj == null) {
            log.info("⚠ No {} data found in process variables. Skipping TaskListener.", entityType);
            return null;
        }
        
        // Validate required fields
        ValidationUtils.requireNonEmpty(processInstanceId, "ProcessInstanceId is required");
        
        if (editedBy == null || editedBy.isEmpty()) {
            log.warn("⚠ Task assignee is null, using 'system' as default");
            editedBy = "system";
        }
        
        return new TaskContext(processInstanceId, formKey, editedBy, dataObj);
    }
    
    /**
     * Process first submission - create new sheet
     */
    private <T> void processFirstSubmission(
            DelegateTask delegateTask,
            List<T> incomingData,
            TaskContext context,
            String entityType,
            String sheetType) {
        
        log.info("✓ Creating new sheet for process");
        
        SheetDto sheet = sheetService.createSheet(context.processInstanceId, sheetType, context.editedBy);
        String sheetId = sheet.getSheetId();
        String variableName = context.formKey + "-sheetId";
        
        log.info("✓ Created new sheet: {} for type: {}", sheetId, sheetType);
        
        // Set metadata and save
        // For first submission, createdBy = editedBy (same user creating the record)
        LocalDateTime now = LocalDateTime.now();
        for (T item : incomingData) {
            setMetadata(item, sheetId, context.editedBy, context.editedBy, now, false, null, null, "PENDING");
        }
        
        saveData(incomingData, entityType);
        delegateTask.setVariable(variableName, sheetId);
        
        log.info("✓ Saved {} new {} for sheet {}", incomingData.size(), entityType.toLowerCase() + "s", sheetId);
    }
    
    /**
     * Process resubmission - create new version with approval preservation
     */
    private <T> void processResubmission(
            DelegateTask delegateTask,
            SheetDto existingSheet,
            List<T> incomingData,
            TaskContext context,
            String entityType,
            String sheetType,
            BiPredicate<T, T> hasChangedPredicate,
            Function<T, String> getNameFunction) {
        
        String existingSheetId = existingSheet.getSheetId();
        log.info("✓ Found existing sheet: {} (version {}). Creating new version.", 
                existingSheetId, existingSheet.getVersion());
        
        // Load existing data for comparison
        List<T> existingData = loadExistingData(existingSheetId, entityType);
        log.info("✓ Loaded {} existing {} for comparison", existingData.size(), entityType.toLowerCase() + "s");
        
        // Create new sheet with incremented version
        SheetDto newSheet = sheetService.createSheet(context.processInstanceId, sheetType, context.editedBy);
        String newSheetId = newSheet.getSheetId();
        log.info("✓ Created new sheet: {} (version {})", newSheetId, newSheet.getVersion());
        
        // Process each incoming item: compare, preserve approval if unchanged, set metadata
        LocalDateTime now = LocalDateTime.now();
        for (T incoming : incomingData) {
            processIncomingItem(incoming, existingData, newSheetId, context.editedBy, now,
                    entityType, hasChangedPredicate, getNameFunction);
        }
        
        // Save and update process variable
        saveData(incomingData, entityType);
        delegateTask.setVariable(context.formKey + "-sheetId", newSheetId);
        
        log.info("✓ Saved {} new {} for sheet {}", incomingData.size(), entityType.toLowerCase() + "s", newSheetId);
    }
    
    /**
     * Process a single incoming item: compare with existing, preserve approval if unchanged
     */
    private <T> void processIncomingItem(
            T incoming,
            List<T> existingData,
            String newSheetId,
            String editedBy,
            LocalDateTime now,
            String entityType,
            BiPredicate<T, T> hasChangedPredicate,
            Function<T, String> getNameFunction) {
        
        T matchingExisting = findMatching(existingData, incoming, hasChangedPredicate);
        
        // Preserve createdBy if item exists, otherwise set to current user
        String createdBy = editedBy; // Default: current user created it
        if (matchingExisting != null) {
            // Item exists - preserve original createdBy, fallback to editedBy if null
            String existingCreatedBy = getCreatedBy(matchingExisting);
            if (existingCreatedBy != null && !existingCreatedBy.isEmpty()) {
                createdBy = existingCreatedBy;
            }
        }
        
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
        
        // Set metadata including sheetId, createdBy, and editedBy
        setBasicMetadata(incoming, newSheetId, createdBy, editedBy, now);
    }
    
    /**
     * Context class to hold task information
     */
    private static class TaskContext {
        final String processInstanceId;
        final String formKey;
        final String editedBy;
        final Object dataObj;
        
        TaskContext(String processInstanceId, String formKey, String editedBy, Object dataObj) {
            this.processInstanceId = processInstanceId;
            this.formKey = formKey;
            this.editedBy = editedBy;
            this.dataObj = dataObj;
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
    
    private <T> void setMetadata(T item, String sheetId, String createdBy, String editedBy, LocalDateTime editedAt, 
                                  Boolean approved, String approvedBy, LocalDateTime approvedAt, String status) {
        if (item instanceof ItemStagingDto) {
            ItemStagingDto dto = (ItemStagingDto) item;
            dto.setSheetId(sheetId);
            dto.setCreatedBy(createdBy);
            dto.setEditedBy(editedBy);
            dto.setEditedAt(editedAt);
            dto.setApproved(approved);
            dto.setApprovedBy(approvedBy);
            dto.setApprovedAt(approvedAt);
            dto.setStatus(status);
        } else if (item instanceof PlanStagingDto) {
            PlanStagingDto dto = (PlanStagingDto) item;
            dto.setSheetId(sheetId);
            dto.setCreatedBy(createdBy);
            dto.setEditedBy(editedBy);
            dto.setEditedAt(editedAt);
            dto.setApproved(approved);
            dto.setApprovedBy(approvedBy);
            dto.setApprovedAt(approvedAt);
            dto.setStatus(status);
        } else if (item instanceof ProductStagingDto) {
            ProductStagingDto dto = (ProductStagingDto) item;
            dto.setSheetId(sheetId);
            dto.setCreatedBy(createdBy);
            dto.setEditedBy(editedBy);
            dto.setEditedAt(editedAt);
            dto.setApproved(approved);
            dto.setApprovedBy(approvedBy);
            dto.setApprovedAt(approvedAt);
            dto.setStatus(status);
        }
    }
    
    private <T> void setBasicMetadata(T item, String sheetId, String createdBy, String editedBy, LocalDateTime editedAt) {
        if (item instanceof ItemStagingDto) {
            ItemStagingDto dto = (ItemStagingDto) item;
            dto.setSheetId(sheetId);
            dto.setCreatedBy(createdBy);
            dto.setEditedBy(editedBy);
            dto.setEditedAt(editedAt);
        } else if (item instanceof PlanStagingDto) {
            PlanStagingDto dto = (PlanStagingDto) item;
            dto.setSheetId(sheetId);
            dto.setCreatedBy(createdBy);
            dto.setEditedBy(editedBy);
            dto.setEditedAt(editedAt);
        } else if (item instanceof ProductStagingDto) {
            ProductStagingDto dto = (ProductStagingDto) item;
            dto.setSheetId(sheetId);
            dto.setCreatedBy(createdBy);
            dto.setEditedBy(editedBy);
            dto.setEditedAt(editedAt);
        }
    }
    
    /**
     * Get createdBy from existing item
     */
    private <T> String getCreatedBy(T item) {
        if (item instanceof ItemStagingDto) {
            return ((ItemStagingDto) item).getCreatedBy();
        } else if (item instanceof PlanStagingDto) {
            return ((PlanStagingDto) item).getCreatedBy();
        } else if (item instanceof ProductStagingDto) {
            return ((ProductStagingDto) item).getCreatedBy();
        }
        return null;
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


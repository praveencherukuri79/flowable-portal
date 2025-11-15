package com.example.backend.service;

import com.example.backend.dto.SheetDto;

public interface SheetService {
    
    /**
     * Create a new sheet for a maker task
     * @param processInstanceId The process instance ID
     * @param sheetType The entity type: item, plan, or product
     * @param createdBy Username of the creator
     */
    SheetDto createSheet(String processInstanceId, String sheetType, String createdBy);
    
    /**
     * Get sheet by sheetId
     */
    SheetDto getSheetBySheetId(String sheetId);
    
    /**
     * Approve a sheet (called when checker approves all rows and clicks "Approve Sheet")
     */
    SheetDto approveSheet(String sheetId, String approvedBy, String comments);
    
    /**
     * Approve a sheet and complete the associated Flowable task
     * @param sheetId The sheet ID
     * @param approvedBy Username of approver
     * @param comments Optional comments
     * @param taskId The Flowable task ID to complete
     * @param decision The decision variable (e.g., "APPROVE")
     */
    SheetDto approveSheetAndCompleteTask(String sheetId, String approvedBy, String comments, String taskId, String decision);
    
    /**
     * Find sheet for a specific process instance and sheetType (returns Optional, doesn't throw)
     * @param processInstanceId The process instance ID
     * @param sheetType The entity type: item, plan, or product
     */
    java.util.Optional<SheetDto> findSheetByProcessAndType(String processInstanceId, String sheetType);
    
    /**
     * Get all sheets (for debugging)
     */
    java.util.List<SheetDto> getAllSheets();
}


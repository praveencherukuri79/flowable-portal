package com.example.backend.service;

import java.util.Map;

/**
 * Common service for fetching staging and master data.
 * Handles the business logic for getting sheet + staging/master data for both maker and checker pages.
 */
public interface CommonStagingDataService {
    
    /**
     * Get all data needed for approval page (sheet + staging data)
     * @param processInstanceId The process instance ID
     * @param entityType The entity type: 'item', 'plan', or 'product'
     * @return Map containing sheetId, data (items/plans/products), and sheet metadata
     */
    Map<String, Object> getApprovalData(String processInstanceId, String entityType);
    
    /**
     * Get all data needed for maker edit page
     * If sheet exists: returns staging data
     * If no sheet: returns master data
     * @param processInstanceId The process instance ID
     * @param entityType The type of entity (item, plan, product)
     * @return Map containing sheetId (if exists), data, and isExistingSheet flag
     */
    Map<String, Object> getMakerData(String processInstanceId, String entityType);
}


package com.example.backend.service.impl;

import com.example.backend.dto.ItemStagingDto;
import com.example.backend.dto.PlanStagingDto;
import com.example.backend.dto.ProductStagingDto;
import com.example.backend.dto.SheetDto;
import com.example.backend.service.*;
import com.example.backend.util.EntityTypeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonStagingDataServiceImpl implements CommonStagingDataService {
    
    private final SheetService sheetService;
    private final ItemStagingService itemStagingService;
    private final PlanStagingService planStagingService;
    private final ProductStagingService productStagingService;
    private final ItemService itemService;
    private final PlanService planService;
    private final ProductService productService;
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getApprovalData(String processInstanceId, String entityType) {
        log.info("Getting approval data for processInstanceId: {}, entityType: {}", processInstanceId, entityType);
        
        // Validate entity type
        EntityTypeUtils.validateEntityType(entityType);
        
        // Query Sheet table using processInstanceId + sheetType
        String sheetType = entityType.toLowerCase();
        log.info("Querying Sheet with sheetType: {}", sheetType);
        
        java.util.Optional<SheetDto> sheetOpt = sheetService.findSheetByProcessAndType(processInstanceId, sheetType);
        if (sheetOpt.isEmpty()) {
            throw new RuntimeException("Sheet not found for process: " + processInstanceId + ", sheetType: " + sheetType);
        }
        
        SheetDto sheet = sheetOpt.get();
        String sheetId = sheet.getSheetId();
        log.info("Found sheet: {}", sheetId);
        
        // Step 3: Fetch staging data
        Object stagingData = fetchStagingData(sheetId, entityType);
        
        // Step 4: Build response
        Map<String, Object> response = new HashMap<>();
        response.put("sheetId", sheetId);
        response.put("sheet", sheet);
        response.put(EntityTypeUtils.getPluralForm(entityType), stagingData);
        
        log.info("Successfully fetched approval data for sheetId: {}", sheetId);
        return response;
    }
    
    private Object fetchStagingData(String sheetId, String entityType) {
        switch (entityType.toLowerCase()) {
            case "item":
                List<ItemStagingDto> items = itemStagingService.getItemsBySheetId(sheetId);
                log.info("Fetched {} items for sheetId: {}", items.size(), sheetId);
                return items;
                
            case "plan":
                List<PlanStagingDto> plans = planStagingService.getPlansBySheetId(sheetId);
                log.info("Fetched {} plans for sheetId: {}", plans.size(), sheetId);
                return plans;
                
            case "product":
                List<ProductStagingDto> products = productStagingService.getProductsBySheetId(sheetId);
                log.info("Fetched {} products for sheetId: {}", products.size(), sheetId);
                return products;
                
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMakerData(String processInstanceId, String entityType) {
        log.info("Getting maker data for processInstanceId: {}, entityType: {}", processInstanceId, entityType);
        
        // Validate entity type
        EntityTypeUtils.validateEntityType(entityType);
        
        Map<String, Object> response = new HashMap<>();
        String pluralKey = EntityTypeUtils.getPluralForm(entityType);
        
        // Try to find existing sheet using sheetType (entityType)
        String sheetType = entityType.toLowerCase();
        java.util.Optional<SheetDto> sheetOpt = sheetService.findSheetByProcessAndType(processInstanceId, sheetType);
        SheetDto sheet = sheetOpt.orElse(null);
        
        if (sheet != null) {
            // Sheet exists - load staging data (resubmit/rejection case)
            String sheetId = sheet.getSheetId();
            Object stagingData = fetchStagingData(sheetId, entityType);
            
            response.put("isExistingSheet", true);
            response.put("sheetId", sheetId);
            response.put("sheet", sheet);
            response.put(pluralKey, stagingData);
            
            log.info("Loaded existing staging data for maker");
        } else {
            // No sheet exists - load master data (first time)
            Object masterData = fetchMasterData(entityType);
            
            response.put("isExistingSheet", false);
            response.put("sheetId", null);
            response.put(pluralKey, masterData);
            
            log.info("Loaded master data for maker (first time)");
        }
        
        return response;
    }
    
    private Object fetchMasterData(String entityType) {
        switch (entityType.toLowerCase()) {
            case "item":
                List<?> items = itemService.getItemsBySheet("MASTER");
                log.info("Fetched {} items from MASTER", items.size());
                return items;
                
            case "plan":
                List<?> plans = planService.getPlansBySheet("MASTER");
                log.info("Fetched {} plans from MASTER", plans.size());
                return plans;
                
            case "product":
                List<?> products = productService.getProductsBySheet("MASTER");
                log.info("Fetched {} products from MASTER", products.size());
                return products;
                
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }
}


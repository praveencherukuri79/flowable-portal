package com.example.backend.service.impl;

import com.example.backend.model.*;
import com.example.backend.repository.*;
import com.example.backend.service.DataMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DataMigrationServiceImpl implements DataMigrationService {
    
    private final ProductStagingRepository productStagingRepository;
    private final PlanStagingRepository planStagingRepository;
    private final ItemStagingRepository itemStagingRepository;
    private final ProductRepository productRepository;
    private final PlanRepository planRepository;
    private final ItemRepository itemRepository;
    private final SheetRepository sheetRepository;
    
    @Override
    public void migrateAllStagingToActual(String processInstanceId) {
        log.info("=== Starting migration for processInstanceId: {} ===", processInstanceId);
        
        if (processInstanceId == null || processInstanceId.isEmpty()) {
            throw new IllegalArgumentException("processInstanceId cannot be null or empty");
        }
        
        // Step 1: Get all 3 sheets for this process instance
        Sheet productSheet = sheetRepository.findByProcessInstanceIdAndSheetType(processInstanceId, "product")
                .orElseThrow(() -> new RuntimeException("Product sheet not found for process: " + processInstanceId));
        Sheet planSheet = sheetRepository.findByProcessInstanceIdAndSheetType(processInstanceId, "plan")
                .orElseThrow(() -> new RuntimeException("Plan sheet not found for process: " + processInstanceId));
        Sheet itemSheet = sheetRepository.findByProcessInstanceIdAndSheetType(processInstanceId, "item")
                .orElseThrow(() -> new RuntimeException("Item sheet not found for process: " + processInstanceId));
        
        log.info("Found sheets - Product: {}, Plan: {}, Item: {}", 
                productSheet.getSheetId(), planSheet.getSheetId(), itemSheet.getSheetId());
        
        // Step 2: Migrate each entity type with its corresponding sheetId
        migrateProducts(productSheet.getSheetId());
        migratePlans(planSheet.getSheetId());
        migrateItems(itemSheet.getSheetId());
        
        log.info("=== Completed migration for processInstanceId: {} ===", processInstanceId);
    }
    
    @Override
    public void migrateProducts(String sheetId) {
        log.info("Migrating products from staging for sheetId: {}", sheetId);
        
        // Step 1: Delete ALL existing products from master table (master table = current production state)
        long existingCount = productRepository.count();
        if (existingCount > 0) {
            productRepository.deleteAll();
            log.info("Deleted {} existing products from master table", existingCount);
        }
        
        // Step 2: Collect approved products from staging to master
        List<ProductStaging> stagingProducts = productStagingRepository.findBySheetId(sheetId);
        List<Product> productsToMigrate = new ArrayList<>();
        
        for (ProductStaging staging : stagingProducts) {
            // Only migrate approved products
            if ("APPROVED".equals(staging.getStatus()) && Boolean.TRUE.equals(staging.getApproved())) {
                
                Product product = Product.builder()
                        .sheetId(staging.getSheetId())
                        .productName(staging.getProductName())
                        .rate(staging.getRate())
                        .api(staging.getApi())
                        .effectiveDate(staging.getEffectiveDate())
                        .status(staging.getStatus())
                        .approvedBy(staging.getApprovedBy())
                        .approvedAt(staging.getApprovedAt())
                        .editedBy(staging.getEditedBy())
                        .editedAt(staging.getEditedAt())
                        .comments(staging.getComments())
                        .build();
                
                productsToMigrate.add(product);
            }
        }
        
        // Step 3: Batch save all approved products
        if (!productsToMigrate.isEmpty()) {
            productRepository.saveAll(productsToMigrate);
            log.info("Migrated {} approved products for sheetId: {}", productsToMigrate.size(), sheetId);
        } else {
            log.info("No approved products to migrate for sheetId: {}", sheetId);
        }
        
        // Note: Staging data is preserved as historical audit trail (not deleted)
        log.info("Completed products migration for sheetId: {} (staging data preserved)", sheetId);
    }
    
    @Override
    public void migratePlans(String sheetId) {
        log.info("Migrating plans from staging for sheetId: {}", sheetId);
        
        // Step 1: Delete ALL existing plans from master table (master table = current production state)
        long existingCount = planRepository.count();
        if (existingCount > 0) {
            planRepository.deleteAll();
            log.info("Deleted {} existing plans from master table", existingCount);
        }
        
        // Step 2: Collect approved plans from staging to master
        List<PlanStaging> stagingPlans = planStagingRepository.findBySheetId(sheetId);
        List<Plan> plansToMigrate = new ArrayList<>();
        
        for (PlanStaging staging : stagingPlans) {
            if ("APPROVED".equals(staging.getStatus()) && Boolean.TRUE.equals(staging.getApproved())) {
                
                Plan plan = Plan.builder()
                        .sheetId(staging.getSheetId())
                        .planName(staging.getPlanName())
                        .planType(staging.getPlanType())
                        .premium(staging.getPremium())
                        .coverageAmount(staging.getCoverageAmount())
                        .effectiveDate(staging.getEffectiveDate())
                        .status(staging.getStatus())
                        .approvedBy(staging.getApprovedBy())
                        .approvedAt(staging.getApprovedAt())
                        .editedBy(staging.getEditedBy())
                        .editedAt(staging.getEditedAt())
                        .comments(staging.getComments())
                        .build();
                
                plansToMigrate.add(plan);
            }
        }
        
        // Step 3: Batch save all approved plans
        if (!plansToMigrate.isEmpty()) {
            planRepository.saveAll(plansToMigrate);
            log.info("Migrated {} approved plans for sheetId: {}", plansToMigrate.size(), sheetId);
        } else {
            log.info("No approved plans to migrate for sheetId: {}", sheetId);
        }
        
        // Note: Staging data is preserved as historical audit trail (not deleted)
        log.info("Completed plans migration for sheetId: {} (staging data preserved)", sheetId);
    }
    
    @Override
    public void migrateItems(String sheetId) {
        log.info("Migrating items from staging for sheetId: {}", sheetId);
        
        // Step 1: Delete ALL existing items from master table (master table = current production state)
        long existingCount = itemRepository.count();
        if (existingCount > 0) {
            itemRepository.deleteAll();
            log.info("Deleted {} existing items from master table", existingCount);
        }
        
        // Step 2: Collect approved items from staging to master
        List<ItemStaging> stagingItems = itemStagingRepository.findBySheetId(sheetId);
        List<Item> itemsToMigrate = new ArrayList<>();
        
        for (ItemStaging staging : stagingItems) {
            if ("APPROVED".equals(staging.getStatus()) && Boolean.TRUE.equals(staging.getApproved())) {
                
                Item item = Item.builder()
                        .sheetId(staging.getSheetId())
                        .itemName(staging.getItemName())
                        .itemCategory(staging.getItemCategory())
                        .price(staging.getPrice())
                        .quantity(staging.getQuantity())
                        .effectiveDate(staging.getEffectiveDate())
                        .status(staging.getStatus())
                        .approvedBy(staging.getApprovedBy())
                        .approvedAt(staging.getApprovedAt())
                        .editedBy(staging.getEditedBy())
                        .editedAt(staging.getEditedAt())
                        .comments(staging.getComments())
                        .build();
                
                itemsToMigrate.add(item);
            }
        }
        
        // Step 3: Batch save all approved items
        if (!itemsToMigrate.isEmpty()) {
            itemRepository.saveAll(itemsToMigrate);
            log.info("Migrated {} approved items for sheetId: {}", itemsToMigrate.size(), sheetId);
        } else {
            log.info("No approved items to migrate for sheetId: {}", sheetId);
        }
        
        // Note: Staging data is preserved as historical audit trail (not deleted)
        log.info("Completed items migration for sheetId: {} (staging data preserved)", sheetId);
    }
}




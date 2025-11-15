package com.example.backend.controller;

import com.example.backend.dto.ApprovalRequest;
import com.example.backend.dto.ItemStagingDto;
import com.example.backend.dto.PlanStagingDto;
import com.example.backend.dto.ProductStagingDto;
import com.example.backend.dto.SheetDto;
import com.example.backend.model.Item;
import com.example.backend.model.Plan;
import com.example.backend.model.Product;
import com.example.backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Read-only controller for querying saved business data (both actual and staging tables).
 * All write operations go through Flowable task completion + task listeners.
 */
@RestController
@RequestMapping("/api/data-query")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Data Query", description = "Read-only APIs for querying saved business data")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
public class DataQueryController {
    
    private final ProductService productService;
    private final PlanService planService;
    private final ItemService itemService;
    private final ProductStagingService productStagingService;
    private final PlanStagingService planStagingService;
    private final ItemStagingService itemStagingService;
    private final SheetService sheetService;
    private final CommonStagingDataService commonStagingDataService;
    
    @GetMapping("/products/sheet/{sheetId}")
    @Operation(summary = "Get products by sheet", description = "Query saved products for a specific sheet")
    public ResponseEntity<List<Product>> getProductsBySheet(@PathVariable String sheetId) {
        return ResponseEntity.ok(productService.getProductsBySheet(sheetId));
    }
    
    @GetMapping("/plans/sheet/{sheetId}")
    @Operation(summary = "Get plans by sheet", description = "Query saved plans for a specific sheet")
    public ResponseEntity<List<Plan>> getPlansBySheet(@PathVariable String sheetId) {
        return ResponseEntity.ok(planService.getPlansBySheet(sheetId));
    }
    
    @GetMapping("/items/sheet/{sheetId}")
    @Operation(summary = "Get items by sheet", description = "Query saved items for a specific sheet")
    public ResponseEntity<List<Item>> getItemsBySheet(@PathVariable String sheetId) {
        return ResponseEntity.ok(itemService.getItemsBySheet(sheetId));
    }
    
    // Staging table queries
    
    @GetMapping("/products/staging/{sheetId}")
    @Operation(summary = "Get staging products by sheet", description = "Query staging products for a specific sheet")
    public ResponseEntity<List<ProductStagingDto>> getProductsStagingBySheet(@PathVariable String sheetId) {
        return ResponseEntity.ok(productStagingService.getProductsBySheetId(sheetId));
    }
    
    @GetMapping("/plans/staging/{sheetId}")
    @Operation(summary = "Get staging plans by sheet", description = "Query staging plans for a specific sheet")
    public ResponseEntity<List<PlanStagingDto>> getPlansStagingBySheet(@PathVariable String sheetId) {
        return ResponseEntity.ok(planStagingService.getPlansBySheetId(sheetId));
    }
    
    @GetMapping("/items/staging/{sheetId}")
    @Operation(summary = "Get staging items by sheet", description = "Query staging items for a specific sheet")
    public ResponseEntity<List<ItemStagingDto>> getItemsStagingBySheet(@PathVariable String sheetId) {
        return ResponseEntity.ok(itemStagingService.getItemsBySheetId(sheetId));
    }
    
    // Approval endpoints for Checkers
    
    @PostMapping("/products/staging/approve-individual/{id}")
    @Operation(summary = "Approve individual product", description = "Mark a single staging product as approved")
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN')")
    public ResponseEntity<Void> approveIndividualProduct(@PathVariable Long id, @RequestBody ApprovalRequest request) {
        productStagingService.approveRow(id, request.getApproverUsername());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/products/staging/approve-bulk/{sheetId}")
    @Operation(summary = "Approve all products", description = "Mark all staging products for sheet as approved")
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN')")
    public ResponseEntity<Void> approveBulkProducts(@PathVariable String sheetId, @RequestBody ApprovalRequest request) {
        productStagingService.approveAllRows(sheetId, request.getApproverUsername());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/plans/staging/approve-individual/{id}")
    @Operation(summary = "Approve individual plan", description = "Mark a single staging plan as approved")
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN')")
    public ResponseEntity<Void> approveIndividualPlan(@PathVariable Long id, @RequestBody ApprovalRequest request) {
        planStagingService.approveRow(id, request.getApproverUsername());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/plans/staging/approve-bulk/{sheetId}")
    @Operation(summary = "Approve all plans", description = "Mark all staging plans for sheet as approved")
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN')")
    public ResponseEntity<Void> approveBulkPlans(@PathVariable String sheetId, @RequestBody ApprovalRequest request) {
        planStagingService.approveAllRows(sheetId, request.getApproverUsername());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/items/staging/approve-individual/{id}")
    @Operation(summary = "Approve individual item", description = "Mark a single staging item as approved")
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN')")
    public ResponseEntity<Void> approveIndividualItem(@PathVariable Long id, @RequestBody ApprovalRequest request) {
        itemStagingService.approveRow(id, request.getApproverUsername());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/items/staging/approve-bulk/{sheetId}")
    @Operation(summary = "Approve all items", description = "Mark all staging items for sheet as approved")
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN')")
    public ResponseEntity<Void> approveBulkItems(@PathVariable String sheetId, @RequestBody ApprovalRequest request) {
        itemStagingService.approveAllRows(sheetId, request.getApproverUsername());
        return ResponseEntity.ok().build();
    }
    
    // Sheet Management Endpoints
    
    @GetMapping("/sheets/{sheetId}")
    @Operation(summary = "Get sheet by ID", description = "Get sheet details by sheetId")
    public ResponseEntity<SheetDto> getSheet(@PathVariable String sheetId) {
        return ResponseEntity.ok(sheetService.getSheetBySheetId(sheetId));
    }
    
    /**
     * Generic endpoint for Checker approval pages.
     * Returns sheetId, staging data (items/plans/products), and sheet metadata in one call.
     * 
     * @param processInstanceId The process instance ID
     * @param entityType The entity type: 'item', 'plan', or 'product'
     * @return Map with keys: sheetId, items/plans/products, sheet
     */
    @GetMapping("/approval-data/{processInstanceId}")
    @Operation(summary = "Get approval data", description = "Generic endpoint to get all data needed for approval page")
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getApprovalData(
            @PathVariable String processInstanceId,
            @RequestParam String entityType) {
        
        log.info("=== GET APPROVAL DATA ===");
        log.info("processInstanceId: {}", processInstanceId);
        log.info("entityType: {}", entityType);
        
        try {
            Map<String, Object> data = commonStagingDataService.getApprovalData(processInstanceId, entityType);
            log.info("✓ Successfully fetched approval data");
            return ResponseEntity.ok(data);
            
        } catch (IllegalArgumentException e) {
            // Validation error (e.g., invalid entity type)
            log.error("✗ Validation error in getApprovalData", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Validation Error",
                "message", e.getMessage(),
                "processInstanceId", processInstanceId,
                "entityType", entityType
            ));
            
        } catch (RuntimeException e) {
            // Check if it's a "not found" exception
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                log.error("✗ Sheet not found: processInstanceId={}, entityType={}", 
                        processInstanceId, entityType, e);
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Sheet Not Found",
                    "message", e.getMessage(),
                    "processInstanceId", processInstanceId,
                    "entityType", entityType,
                    "hint", "The sheet may not have been created yet. Maker must complete their task first."
                ));
            } else {
                // Other runtime errors
                log.error("✗ Runtime error in getApprovalData: processInstanceId={}, entityType={}", 
                        processInstanceId, entityType, e);
                return ResponseEntity.status(500).body(Map.of(
                    "error", "Runtime Error",
                    "message", e.getMessage(),
                    "type", e.getClass().getSimpleName(),
                    "processInstanceId", processInstanceId,
                    "entityType", entityType
                ));
            }
            
        } catch (Exception e) {
            log.error("✗ Unexpected error in getApprovalData: processInstanceId={}, entityType={}", 
                    processInstanceId, entityType, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage(),
                "type", e.getClass().getSimpleName(),
                "processInstanceId", processInstanceId,
                "entityType", entityType
            ));
        }
    }
    
    /**
     * Generic endpoint for Maker edit pages.
     * If sheet exists (resubmit/rejection case): returns staging data with approval status
     * If no sheet (first time): returns master data
     * 
     * @param processInstanceId The process instance ID
     * @param entityType The entity type: 'item', 'plan', or 'product'
     * @return Map with keys: isExistingSheet, sheetId, items/plans/products, sheet (if exists)
     */
    @GetMapping("/maker-data/{processInstanceId}")
    @Operation(summary = "Get maker data", description = "Generic endpoint to get all data needed for maker edit page (staging if exists, else master)")
    @PreAuthorize("hasAnyRole('MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getMakerData(
            @PathVariable String processInstanceId,
            @RequestParam String entityType) {
        try {
            Map<String, Object> data = commonStagingDataService.getMakerData(processInstanceId, entityType);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/debug/sheets")
    @Operation(summary = "DEBUG: List all sheets", description = "Temporary debug endpoint to see all sheets")
    public ResponseEntity<List<SheetDto>> debugListAllSheets() {
        try {
            List<SheetDto> sheets = sheetService.getAllSheets();
            log.info(">>> DEBUG: Found {} sheets in database", sheets.size());
            sheets.forEach(sheet -> log.info(">>> Sheet: id={}, processInstanceId={}, sheetType={}", 
                sheet.getSheetId(), sheet.getProcessInstanceId(), sheet.getSheetType()));
            return ResponseEntity.ok(sheets);
        } catch (Exception e) {
            log.error(">>> DEBUG: Error listing sheets", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/sheets/{sheetId}/approve")
    @Operation(summary = "Approve sheet and complete task", description = "Mark the sheet as approved and complete the Flowable task")
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN')")
    public ResponseEntity<SheetDto> approveSheet(
            @PathVariable String sheetId, 
            @RequestBody Map<String, String> request) {
        String approvedBy = request.get("approvedBy");
        String comments = request.get("comments");
        String taskId = request.get("taskId");
        String decision = request.get("decision");
        
        SheetDto approved = sheetService.approveSheetAndCompleteTask(sheetId, approvedBy, comments, taskId, decision);
        return ResponseEntity.ok(approved);
    }
    
    @GetMapping("/sheets/check-rows-approved/{sheetId}")
    @Operation(summary = "Check if all rows approved", description = "Check if all staging rows for a sheet are approved")
    public ResponseEntity<Map<String, Object>> checkAllRowsApproved(
            @PathVariable String sheetId,
            @RequestParam String entityType) {
        boolean allApproved = false;
        
        if ("products".equalsIgnoreCase(entityType)) {
            allApproved = productStagingService.areAllRowsApproved(sheetId);
        } else if ("plans".equalsIgnoreCase(entityType)) {
            allApproved = planStagingService.areAllRowsApproved(sheetId);
        } else if ("items".equalsIgnoreCase(entityType)) {
            allApproved = itemStagingService.areAllRowsApproved(sheetId);
        }
        
        return ResponseEntity.ok(Map.of("allApproved", allApproved));
    }
}


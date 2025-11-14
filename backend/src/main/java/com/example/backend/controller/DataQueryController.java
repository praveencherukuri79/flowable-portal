package com.example.backend.controller;

import com.example.backend.model.Item;
import com.example.backend.model.Plan;
import com.example.backend.model.Product;
import com.example.backend.service.ItemService;
import com.example.backend.service.PlanService;
import com.example.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only controller for querying saved business data.
 * All write operations go through Flowable task completion + task listeners.
 */
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@Tag(name = "Data Query", description = "Read-only APIs for querying saved business data")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'ADMIN')")
public class DataQueryController {
    
    private final ProductService productService;
    private final PlanService planService;
    private final ItemService itemService;
    
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
}


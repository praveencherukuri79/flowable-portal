package com.example.backend.service;

import com.example.backend.dto.ProductStagingDto;
import java.util.List;

public interface ProductStagingService {
    List<ProductStagingDto> getProductsBySheetId(String sheetId);
    ProductStagingDto saveProduct(ProductStagingDto dto);
    List<ProductStagingDto> saveProducts(List<ProductStagingDto> dtos);
    
    // Simplified approval methods
    void approveRow(Long id, String approverUsername);
    void approveAllRows(String sheetId, String approverUsername);
    boolean areAllRowsApproved(String sheetId);
    
    void deleteBySheetId(String sheetId);
}


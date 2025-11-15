package com.example.backend.service;

import com.example.backend.dto.ItemStagingDto;
import java.util.List;

public interface ItemStagingService {
    List<ItemStagingDto> getItemsBySheetId(String sheetId);
    ItemStagingDto saveItem(ItemStagingDto dto);
    List<ItemStagingDto> saveItems(List<ItemStagingDto> dtos);
    
    // Simplified approval methods
    void approveRow(Long id, String approverUsername);
    void approveAllRows(String sheetId, String approverUsername);
    boolean areAllRowsApproved(String sheetId);
    
    void deleteBySheetId(String sheetId);
}


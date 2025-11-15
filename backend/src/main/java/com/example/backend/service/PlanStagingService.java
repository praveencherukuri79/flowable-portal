package com.example.backend.service;

import com.example.backend.dto.PlanStagingDto;
import java.util.List;

public interface PlanStagingService {
    List<PlanStagingDto> getPlansBySheetId(String sheetId);
    PlanStagingDto savePlan(PlanStagingDto dto);
    List<PlanStagingDto> savePlans(List<PlanStagingDto> dtos);
    
    // Simplified approval methods
    void approveRow(Long id, String approverUsername);
    void approveAllRows(String sheetId, String approverUsername);
    boolean areAllRowsApproved(String sheetId);
    
    void deleteBySheetId(String sheetId);
}


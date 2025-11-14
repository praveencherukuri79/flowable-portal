package com.example.backend.service;

import com.example.backend.dto.PlanDto;
import com.example.backend.model.Plan;

import java.util.List;

public interface PlanService {
    Plan createPlan(PlanDto planDto);
    Plan updatePlan(Long id, PlanDto planDto);
    void deletePlan(Long id);
    Plan getPlan(Long id);
    List<Plan> getPlansBySheet(String sheetId);
    void approvePlan(Long planId, String approvedBy);
    void rejectPlan(Long planId, String approvedBy, String comments);
    void approveAllPlans(String sheetId, String approvedBy);
    List<Plan> savePlansFromTask(String sheetId, List<PlanDto> plans, String editedBy);
}


package com.example.backend.service.impl;

import com.example.backend.dto.PlanDto;
import com.example.backend.model.Plan;
import com.example.backend.repository.PlanRepository;
import com.example.backend.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanServiceImpl implements PlanService {
    
    private final PlanRepository planRepository;
    
    @Override
    @Transactional
    public Plan createPlan(PlanDto dto) {
        Plan plan = Plan.builder()
                .sheetId(dto.getSheetId())
                .planName(dto.getPlanName())
                .planType(dto.getPlanType())
                .premium(dto.getPremium())
                .coverageAmount(dto.getCoverageAmount())
                .effectiveDate(dto.getEffectiveDate())
                .status("PENDING")
                .editedBy(dto.getEditedBy())
                .editedAt(LocalDateTime.now())
                .comments(dto.getComments())
                .build();
        
        return planRepository.save(plan);
    }
    
    @Override
    @Transactional
    public Plan updatePlan(Long id, PlanDto dto) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + id));
        
        plan.setPlanName(dto.getPlanName());
        plan.setPlanType(dto.getPlanType());
        plan.setPremium(dto.getPremium());
        plan.setCoverageAmount(dto.getCoverageAmount());
        plan.setEffectiveDate(dto.getEffectiveDate());
        plan.setEditedBy(dto.getEditedBy());
        plan.setEditedAt(LocalDateTime.now());
        plan.setComments(dto.getComments());
        
        return planRepository.save(plan);
    }
    
    @Override
    @Transactional
    public void deletePlan(Long id) {
        planRepository.deleteById(id);
    }
    
    @Override
    public Plan getPlan(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + id));
    }
    
    @Override
    public List<Plan> getPlansBySheet(String sheetId) {
        return planRepository.findBySheetId(sheetId);
    }
    
    @Override
    @Transactional
    public void approvePlan(Long planId, String approvedBy) {
        Plan plan = getPlan(planId);
        plan.setStatus("APPROVED");
        plan.setApprovedBy(approvedBy);
        plan.setApprovedAt(LocalDateTime.now());
        planRepository.save(plan);
        
        log.info("Plan {} approved by {}", planId, approvedBy);
    }
    
    @Override
    @Transactional
    public void rejectPlan(Long planId, String approvedBy, String comments) {
        Plan plan = getPlan(planId);
        plan.setStatus("REJECTED");
        plan.setApprovedBy(approvedBy);
        plan.setApprovedAt(LocalDateTime.now());
        plan.setComments(comments);
        planRepository.save(plan);
        
        log.info("Plan {} rejected by {}", planId, approvedBy);
    }
    
    @Override
    @Transactional
    public void approveAllPlans(String sheetId, String approvedBy) {
        List<Plan> plans = planRepository.findBySheetIdAndStatus(sheetId, "PENDING");
        
        plans.forEach(plan -> {
            plan.setStatus("APPROVED");
            plan.setApprovedBy(approvedBy);
            plan.setApprovedAt(LocalDateTime.now());
        });
        
        planRepository.saveAll(plans);
        
        log.info("Approved all {} plans for sheet {} by {}", plans.size(), sheetId, approvedBy);
    }
    
    @Override
    @Transactional
    public List<Plan> savePlansFromTask(String sheetId, List<PlanDto> planDtos, String editedBy) {
        // Delete existing plans for this sheet
        List<Plan> existingPlans = planRepository.findBySheetId(sheetId);
        planRepository.deleteAll(existingPlans);
        
        // Create new plans
        List<Plan> plans = planDtos.stream()
                .map(dto -> Plan.builder()
                        .sheetId(sheetId)
                        .planName(dto.getPlanName())
                        .planType(dto.getPlanType())
                        .premium(dto.getPremium())
                        .coverageAmount(dto.getCoverageAmount())
                        .effectiveDate(dto.getEffectiveDate())
                        .status("PENDING")
                        .editedBy(editedBy)
                        .editedAt(LocalDateTime.now())
                        .comments(dto.getComments())
                        .build())
                .collect(Collectors.toList());
        
        List<Plan> savedPlans = planRepository.saveAll(plans);
        
        log.info("Saved {} plans for sheet {} by {}", savedPlans.size(), sheetId, editedBy);
        
        return savedPlans;
    }
}


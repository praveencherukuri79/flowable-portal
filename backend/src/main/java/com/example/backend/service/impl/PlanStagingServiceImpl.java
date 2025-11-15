package com.example.backend.service.impl;

import com.example.backend.dto.PlanStagingDto;
import com.example.backend.model.PlanStaging;
import com.example.backend.repository.PlanStagingRepository;
import com.example.backend.service.PlanStagingService;
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
@Transactional
public class PlanStagingServiceImpl implements PlanStagingService {
    
    private final PlanStagingRepository repository;
    
    @Override
    public List<PlanStagingDto> getPlansBySheetId(String sheetId) {
        return repository.findBySheetId(sheetId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public PlanStagingDto savePlan(PlanStagingDto dto) {
        PlanStaging entity = toEntity(dto);
        entity = repository.save(entity);
        return toDto(entity);
    }
    
    @Override
    public List<PlanStagingDto> savePlans(List<PlanStagingDto> dtos) {
        List<PlanStaging> entities = dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        entities = repository.saveAll(entities);
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public void approveRow(Long id, String approverUsername) {
        PlanStaging plan = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found with id: " + id));
        
        plan.setApproved(true);
        plan.setApprovedBy(approverUsername);
        plan.setApprovedAt(LocalDateTime.now());
        plan.setStatus("APPROVED");
        repository.save(plan);
        
        log.info("✓ Approved plan {} by {}", plan.getPlanName(), approverUsername);
    }
    
    @Override
    public void approveAllRows(String sheetId, String approverUsername) {
        if (sheetId == null || sheetId.isEmpty()) {
            throw new IllegalArgumentException("SheetId cannot be null or empty");
        }
        if (approverUsername == null || approverUsername.isEmpty()) {
            throw new IllegalArgumentException("Approver username cannot be null or empty");
        }
        
        List<PlanStaging> plans = repository.findBySheetId(sheetId);
        if (plans.isEmpty()) {
            log.warn("No plans found for sheetId: {}", sheetId);
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        plans.forEach(plan -> {
            plan.setApproved(true);
            plan.setApprovedBy(approverUsername);
            plan.setApprovedAt(now);
            plan.setStatus("APPROVED");
        });
        repository.saveAll(plans);
        
        log.info("✓ Bulk approved {} plans for sheet {} by {}", plans.size(), sheetId, approverUsername);
    }
    
    @Override
    public boolean areAllRowsApproved(String sheetId) {
        List<PlanStaging> plans = repository.findBySheetId(sheetId);
        return !plans.isEmpty() && plans.stream().allMatch(p -> Boolean.TRUE.equals(p.getApproved()));
    }
    
    @Override
    public void deleteBySheetId(String sheetId) {
        repository.deleteBySheetId(sheetId);
    }
    
    private PlanStagingDto toDto(PlanStaging entity) {
        return PlanStagingDto.builder()
                .id(entity.getId())
                .sheetId(entity.getSheetId())
                .planName(entity.getPlanName())
                .planType(entity.getPlanType())
                .premium(entity.getPremium())
                .coverageAmount(entity.getCoverageAmount())
                .effectiveDate(entity.getEffectiveDate())
                .status(entity.getStatus())
                .approved(entity.getApproved())
                .approvedBy(entity.getApprovedBy())
                .approvedAt(entity.getApprovedAt())
                .editedBy(entity.getEditedBy())
                .editedAt(entity.getEditedAt())
                .comments(entity.getComments())
                .build();
    }
    
    private PlanStaging toEntity(PlanStagingDto dto) {
        return PlanStaging.builder()
                .id(dto.getId())
                .sheetId(dto.getSheetId())
                .planName(dto.getPlanName())
                .planType(dto.getPlanType())
                .premium(dto.getPremium())
                .coverageAmount(dto.getCoverageAmount())
                .effectiveDate(dto.getEffectiveDate())
                .status(dto.getStatus())
                .approved(dto.getApproved())
                .approvedBy(dto.getApprovedBy())
                .approvedAt(dto.getApprovedAt())
                .editedBy(dto.getEditedBy())
                .editedAt(dto.getEditedAt())
                .comments(dto.getComments())
                .build();
    }
}


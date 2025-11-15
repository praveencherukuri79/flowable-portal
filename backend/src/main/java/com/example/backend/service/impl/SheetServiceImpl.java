package com.example.backend.service.impl;

import com.example.backend.dto.SheetDto;
import com.example.backend.model.Sheet;
import com.example.backend.repository.SheetRepository;
import com.example.backend.service.SheetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SheetServiceImpl implements SheetService {
    
    private final SheetRepository sheetRepository;
    private final TaskService taskService;
    
    @Override
    public SheetDto createSheet(String processInstanceId, String sheetType, String createdBy) {
        String sheetId = "SHEET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Sheet sheet = Sheet.builder()
                .sheetId(sheetId)
                .sheetType(sheetType.toLowerCase())
                .processInstanceId(processInstanceId)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .status("PENDING")
                .build();
        
        sheet = sheetRepository.save(sheet);
        log.info("✓ Created new sheet: {} for type: {} in process: {}", sheetId, sheetType, processInstanceId);
        
        return mapToDto(sheet);
    }
    
    @Override
    @Transactional(readOnly = true)
    public SheetDto getSheetBySheetId(String sheetId) {
        Sheet sheet = sheetRepository.findBySheetId(sheetId)
                .orElseThrow(() -> new RuntimeException("Sheet not found: " + sheetId));
        return mapToDto(sheet);
    }
    
    @Override
    public SheetDto approveSheet(String sheetId, String approvedBy, String comments) {
        Sheet sheet = sheetRepository.findBySheetId(sheetId)
                .orElseThrow(() -> new RuntimeException("Sheet not found: " + sheetId));
        
        sheet.setApprovedBy(approvedBy);
        sheet.setApprovedAt(LocalDateTime.now());
        sheet.setStatus("APPROVED");
        if (comments != null) {
            sheet.setComments(comments);
        }
        
        sheet = sheetRepository.save(sheet);
        log.info("✓ Sheet approved: {} by {}", sheetId, approvedBy);
        
        return mapToDto(sheet);
    }
    
    @Override
    public SheetDto approveSheetAndCompleteTask(String sheetId, String approvedBy, String comments, String taskId, String decision) {
        log.info("=== Approving sheet and completing task ===");
        log.info("SheetId: {}, TaskId: {}, Decision: {}", sheetId, taskId, decision);
        
        // Step 1: Approve the sheet
        SheetDto sheetDto = approveSheet(sheetId, approvedBy, comments);
        log.info("✓ Sheet approved: {}", sheetId);
        
        // Step 2: Complete the Flowable task with the decision variable
        if (taskId != null && !taskId.isEmpty() && decision != null && !decision.isEmpty()) {
            Map<String, Object> variables = new HashMap<>();
            variables.put(decision, "APPROVE");
            
            taskService.complete(taskId, variables);
            log.info("✓ Task completed: {} with decision: {} = APPROVE", taskId, decision);
        } else {
            log.warn("⚠ TaskId or decision is missing. Task not completed. TaskId: {}, Decision: {}", taskId, decision);
        }
        
        return sheetDto;
    }
    
    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<SheetDto> findSheetByProcessAndType(String processInstanceId, String sheetType) {
        return sheetRepository.findByProcessInstanceIdAndSheetType(processInstanceId, sheetType.toLowerCase())
                .map(this::mapToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public java.util.List<SheetDto> getAllSheets() {
        return sheetRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private SheetDto mapToDto(Sheet sheet) {
        SheetDto dto = new SheetDto();
        dto.setId(sheet.getId());
        dto.setSheetId(sheet.getSheetId());
        dto.setSheetType(sheet.getSheetType());
        dto.setProcessInstanceId(sheet.getProcessInstanceId());
        dto.setCreatedBy(sheet.getCreatedBy());
        dto.setCreatedAt(sheet.getCreatedAt());
        dto.setApprovedBy(sheet.getApprovedBy());
        dto.setApprovedAt(sheet.getApprovedAt());
        dto.setStatus(sheet.getStatus());
        dto.setComments(sheet.getComments());
        return dto;
    }
}


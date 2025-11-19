package com.example.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SheetDto {
    private Long id;
    private String sheetId;
    private String sheetType; // item, plan, or product
    private String processInstanceId;
    private Integer version; // Version number for this processInstanceId + sheetType combination
    private String createdBy;
    private LocalDateTime createdAt;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String status;
    private String comments;
}


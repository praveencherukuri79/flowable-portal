package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemStagingDto {
    private Long id;
    private String sheetId;
    private String itemName;
    private String itemCategory;
    private Double price;
    private Integer quantity;
    private LocalDate effectiveDate;
    private String status;
    private Boolean approved;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String createdBy; // User who first created this record
    private String editedBy; // User who last edited this record
    private LocalDateTime editedAt;
    private String comments;
}


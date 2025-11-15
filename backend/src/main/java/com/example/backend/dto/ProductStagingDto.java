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
public class ProductStagingDto {
    private Long id;
    private String sheetId;
    private String productName;
    private Double rate;
    private String api;
    private LocalDate effectiveDate;
    private String status;
    private Boolean approved;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String editedBy;
    private LocalDateTime editedAt;
    private String comments;
}


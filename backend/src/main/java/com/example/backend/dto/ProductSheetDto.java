package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSheetDto {
    private Long id;
    private String sheetId;
    private String processInstanceId;
    private Integer currentStage;
    private String stage1Status;
    private String stage2Status;
    private String stage3Status;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<ProductDto> products;
}


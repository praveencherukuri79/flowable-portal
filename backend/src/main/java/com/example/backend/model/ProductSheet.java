package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_sheets")
public class ProductSheet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String sheetId;
    
    @Column(nullable = false)
    private String processInstanceId;
    
    @Column(nullable = false)
    private Integer currentStage; // 1, 2, or 3
    
    @Column
    private String stage1Status; // PENDING, IN_PROGRESS, APPROVED, REJECTED
    
    @Column
    private String stage2Status;
    
    @Column
    private String stage3Status;
    
    @Column
    private String stage1MakerTaskId;
    
    @Column
    private String stage1CheckerTaskId;
    
    @Column
    private String stage2MakerTaskId;
    
    @Column
    private String stage2CheckerTaskId;
    
    @Column
    private String stage3MakerTaskId;
    
    @Column
    private String stage3CheckerTaskId;
    
    @Column
    private String createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currentStage == null) {
            currentStage = 1;
        }
        if (stage1Status == null) {
            stage1Status = "PENDING";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


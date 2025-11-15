package com.example.backend.model;

import jakarta.persistence.*;
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
@Entity
@Table(name = "plan_staging")
public class PlanStaging {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String sheetId;
    
    @Column(nullable = false)
    private String planName;
    
    @Column(nullable = false)
    private String planType;
    
    @Column(nullable = false)
    private Double premium;
    
    @Column(nullable = false)
    private Integer coverageAmount;
    
    @Column(nullable = false)
    private LocalDate effectiveDate;
    
    @Column
    private String status; // PENDING, APPROVED, REJECTED
    
    // Approval tracking (simplified - no individual/bulk distinction)
    @Builder.Default
    @Column
    private Boolean approved = false;
    
    @Column
    private String approvedBy;
    
    @Column
    private LocalDateTime approvedAt;
    
    @Column
    private String editedBy;
    
    @Column
    private LocalDateTime editedAt;
    
    @Column(length = 1000)
    private String comments;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


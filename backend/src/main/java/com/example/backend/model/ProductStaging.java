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
@Table(name = "product_staging")
public class ProductStaging {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String sheetId;
    
    @Column(nullable = false)
    private String productName;
    
    @Column(nullable = false)
    private Double rate;
    
    @Column(nullable = false)
    private String api;
    
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
    private String createdBy; // User who first created this record
    
    @Column
    private String editedBy; // User who last edited this record
    
    @Column
    private LocalDateTime editedAt;
    
    @Column(length = 1000)
    private String comments;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}


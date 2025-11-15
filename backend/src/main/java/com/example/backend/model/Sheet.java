package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Central sheet management table.
 * One sheet is created for each maker task in the process.
 * Tracks approval status for the entire sheet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sheets")
public class Sheet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String sheetId;
    
    @Column(nullable = false)
    private String sheetType; // item, plan, or product
    
    @Column(nullable = false)
    private String processInstanceId;
    
    @Column(nullable = false)
    private String createdBy;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private String approvedBy;
    
    @Column
    private LocalDateTime approvedAt;
    
    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED
    
    @Column
    private String comments;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


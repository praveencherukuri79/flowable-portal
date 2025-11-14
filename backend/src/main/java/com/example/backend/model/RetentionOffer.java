package com.example.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "retention_offers")
@Schema(description = "Retention offer entity with financial details")
public class RetentionOffer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier", example = "1")
    private Long id;
    
    @Column(nullable = false)
    @Schema(description = "Username of the customer", example = "john.doe", required = true)
    private String username;
    
    @Column(columnDefinition = "TEXT")
    @Schema(description = "Detailed offer information")
    private String offerDetails;
    
    @Column(nullable = false)
    @Schema(description = "Current offer status", example = "PENDING", required = true)
    private String status;
    
    @Column(precision = 5, scale = 2)
    @Schema(description = "Interest rate percentage", example = "2.50")
    private BigDecimal rate;
    
    @Column(name = "annual_percentage_yield", precision = 5, scale = 2)
    @Schema(description = "Annual Percentage Yield", example = "2.53")
    private BigDecimal apy;
    
    @Column(name = "effective_date")
    @Schema(description = "Effective date of the offer", example = "2024-01-15")
    private LocalDate effectiveDate;
    
    @Column(name = "created_at", nullable = false)
    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @Schema(description = "Last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

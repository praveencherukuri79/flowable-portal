package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Process instance information with comprehensive details")
public class ProcessInstanceDto {
    
    @Schema(description = "Process instance ID", example = "12345")
    public String id;
    
    @Schema(description = "Process definition ID", example = "retentionOffer:1:67890")
    public String processDefinitionId;
    
    @Schema(description = "Process definition key", example = "retentionOffer")
    public String processDefinitionKey;
    
    @Schema(description = "Business key for the process", example = "OFFER-2024-001")
    public String businessKey;
    
    @Schema(description = "User who started the process", example = "john.doe")
    public String startUserId;
    
    @Schema(description = "Process start time", example = "2024-01-15T10:30:00Z")
    public String startTime;
    
    @Schema(description = "Process end time", example = "2024-01-15T12:45:00Z")
    public String endTime;
    
    @Schema(description = "Current process status", example = "ACTIVE")
    public String status;
    
    @Schema(description = "Process variables")
    public Map<String, Object> variables;
    
    @Schema(description = "Currently active activity IDs")
    public List<String> activeActivityIds;
    
    @Schema(description = "Completed activity IDs")
    public List<String> completedActivityIds;
    
    @Schema(description = "URL to process diagram", example = "/api/diagram/process/12345")
    public String diagramUrl;
    
    @Schema(description = "Tenant ID", example = "default")
    public String tenantId;
    
    @Schema(description = "Suspension status", example = "false")
    public boolean suspended;
    
    @Schema(description = "Process instance name", example = "Retention Offer Process")
    public String name;
    
    @Schema(description = "Process description", example = "Maker-checker workflow for retention offers")
    public String description;
}

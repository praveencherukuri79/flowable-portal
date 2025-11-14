package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Historical process instance information")
public class HistoricProcessInstanceDto {
    
    @Schema(description = "Historic process instance ID", example = "12345")
    public String id;
    
    @Schema(description = "Process definition ID", example = "retentionOffer:1:67890")
    public String processDefinitionId;
    
    @Schema(description = "Process definition key", example = "retentionOffer")
    public String processDefinitionKey;
    
    @Schema(description = "Business key", example = "OFFER-2024-001")
    public String businessKey;
    
    @Schema(description = "User who started the process", example = "john.doe")
    public String startUserId;
    
    @Schema(description = "Process start time")
    public Date startTime;
    
    @Schema(description = "Process end time")
    public Date endTime;
    
    @Schema(description = "Final process status", example = "COMPLETED")
    public String status;
    
    @Schema(description = "Process variables at completion")
    public Map<String, Object> variables;
    
    @Schema(description = "Tenant ID", example = "default")
    public String tenantId;
    
    @Schema(description = "Process instance name", example = "Retention Offer Process")
    public String name;
    
    @Schema(description = "Process description")
    public String description;
}

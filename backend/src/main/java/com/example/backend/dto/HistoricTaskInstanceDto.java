package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Historical task instance information")
public class HistoricTaskInstanceDto {
    
    @Schema(description = "Historic task ID", example = "task123")
    public String id;
    
    @Schema(description = "Task name", example = "Review Retention Offer")
    public String name;
    
    @Schema(description = "Task description")
    public String description;
    
    @Schema(description = "Assigned user", example = "john.doe")
    public String assignee;
    
    @Schema(description = "Task owner", example = "jane.smith")
    public String owner;
    
    @Schema(description = "Process instance ID", example = "12345")
    public String processInstanceId;
    
    @Schema(description = "Process definition ID", example = "retentionOffer:1:67890")
    public String processDefinitionId;
    
    @Schema(description = "Execution ID", example = "exec123")
    public String executionId;
    
    @Schema(description = "Task definition key", example = "approvalTask")
    public String taskDefinitionKey;
    
    @Schema(description = "Task creation time")
    public Date createTime;
    
    @Schema(description = "Task end time")
    public Date endTime;
    
    @Schema(description = "Task duration in milliseconds")
    public Long durationInMillis;
    
    @Schema(description = "Task priority", example = "50")
    public int priority;
    
    @Schema(description = "Task category", example = "approval")
    public String category;
    
    @Schema(description = "Form key", example = "approvalForm")
    public String formKey;
    
    @Schema(description = "Tenant ID", example = "default")
    public String tenantId;
}
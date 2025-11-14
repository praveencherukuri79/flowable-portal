package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Task information with comprehensive details")
public class TaskDto {
    
    @Schema(description = "Task ID", example = "task123")
    public String id;
    
    @Schema(description = "Task name", example = "Review Retention Offer")
    public String name;
    
    @Schema(description = "Task description", example = "Review and approve the retention offer")
    public String description;
    
    @Schema(description = "Assigned user", example = "john.doe")
    public String assignee;
    
    @Schema(description = "Task owner", example = "jane.smith")
    public String owner;
    
    @Schema(description = "Delegation state", example = "PENDING")
    public String delegationState;
    
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
    
    @Schema(description = "Task due date")
    public Date dueDate;
    
    @Schema(description = "Task priority", example = "50")
    public int priority;
    
    @Schema(description = "Task category", example = "approval")
    public String category;
    
    @Schema(description = "Form key", example = "approvalForm")
    public String formKey;
    
    @Schema(description = "Candidate users for the task")
    public List<String> candidateUsers;
    
    @Schema(description = "Candidate groups for the task")
    public List<String> candidateGroups;
    
    @Schema(description = "Task variables")
    public Map<String, Object> variables;
    
    @Schema(description = "Task comments")
    public List<String> comments;
    
    @Schema(description = "Task attachments")
    public List<String> attachments;
    
    @Schema(description = "Suspension status", example = "false")
    public String suspended;
    
    @Schema(description = "Tenant ID", example = "default")
    public String tenantId;
    
    @Schema(description = "Task state", example = "CLAIMABLE or ASSIGNED")
    public String state;
}

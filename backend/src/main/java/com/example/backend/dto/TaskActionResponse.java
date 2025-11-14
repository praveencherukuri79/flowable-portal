package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Task action response (claim, complete, delegate, etc.)")
public class TaskActionResponse {
    
    @Schema(description = "Task ID", example = "task123")
    private String taskId;
    
    @Schema(description = "Action performed", example = "CLAIMED")
    private String action;
    
    @Schema(description = "User who performed the action", example = "john.doe")
    private String performedBy;
    
    @Schema(description = "Response message", example = "Task claimed successfully")
    private String message;
    
    @Schema(description = "Task state after action", example = "ASSIGNED")
    private String taskState;
}


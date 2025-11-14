package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Comment DTO representing a task or process instance comment")
public class CommentDto {
    
    @Schema(description = "Unique identifier of the comment", example = "comment-123")
    private String id;
    
    @Schema(description = "User ID who made the comment", example = "user-456")
    private String userId;
    
    @Schema(description = "Task ID this comment belongs to", example = "task-789")
    private String taskId;
    
    @Schema(description = "Process instance ID this comment belongs to", example = "process-101")
    private String processInstanceId;
    
    @Schema(description = "Comment message content", example = "This task requires additional review")
    private String message;
    
    @Schema(description = "Timestamp when the comment was created")
    private Date timeStamp;
}

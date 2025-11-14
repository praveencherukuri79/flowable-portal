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
@Schema(description = "Attachment DTO representing a file attachment to tasks or process instances")
public class AttachmentDto {
    
    @Schema(description = "Unique identifier of the attachment", example = "attach-123")
    private String id;
    
    @Schema(description = "Name of the attachment", example = "document.pdf")
    private String name;
    
    @Schema(description = "Description of the attachment", example = "Supporting documentation")
    private String description;
    
    @Schema(description = "MIME type of the attachment", example = "application/pdf")
    private String type;
    
    @Schema(description = "Task ID this attachment belongs to", example = "task-789")
    private String taskId;
    
    @Schema(description = "Process instance ID this attachment belongs to", example = "process-101")
    private String processInstanceId;
    
    @Schema(description = "URL to access the attachment", example = "/api/attachments/attach-123")
    private String url;
    
    @Schema(description = "Timestamp when the attachment was created")
    private Date timeStamp;
}

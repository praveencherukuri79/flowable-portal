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
@Schema(description = "Process start response")
public class ProcessStartResponse {
    
    @Schema(description = "Process instance ID", example = "12345")
    private String processInstanceId;
    
    @Schema(description = "Process definition ID", example = "threeStageProcess:1:67890")
    private String processDefinitionId;
    
    @Schema(description = "Process key", example = "threeStageProcess")
    private String processKey;
    
    @Schema(description = "Business key", example = "SHEET-ABC123")
    private String businessKey;
    
    @Schema(description = "Response message", example = "Process started successfully")
    private String message;
}


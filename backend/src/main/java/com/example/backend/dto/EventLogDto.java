package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Event log entry from Flowable")
public class EventLogDto {
    @Schema(description = "Event log ID")
    public String id;
    
    @Schema(description = "Event timestamp")
    public Date timestamp;
    
    @Schema(description = "Event type")
    public String type;
    
    @Schema(description = "Process definition ID")
    public String processDefinitionId;
    
    @Schema(description = "Process instance ID")
    public String processInstanceId;
    
    @Schema(description = "Execution ID")
    public String executionId;
    
    @Schema(description = "Event data")
    public String data;
}


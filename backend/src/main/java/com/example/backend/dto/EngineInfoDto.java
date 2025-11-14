package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Flowable engine information and status")
public class EngineInfoDto {
    
    @Schema(description = "Engine name", example = "Process Engine")
    public String name;
    
    @Schema(description = "Engine version", example = "7.2.0")
    public String version;
    
    @Schema(description = "Resource URL", example = "jdbc:h2:mem:flowable")
    public String resourceUrl;
    
    @Schema(description = "Exception message if any errors")
    public String exception;
}

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
@Schema(description = "BPMN model information with metadata")
public class ModelDto {
    
    @Schema(description = "Model ID", example = "model123")
    public String id;
    
    @Schema(description = "Model name", example = "Retention Offer Process")
    public String name;
    
    @Schema(description = "Model key", example = "retentionOffer")
    public String key;
    
    @Schema(description = "Model category", example = "Banking")
    public String category;
    
    @Schema(description = "Model version", example = "1.0")
    public String version;
    
    @Schema(description = "Model metadata information")
    public String metaInfo;
    
    @Schema(description = "Model creation time")
    public Date createTime;
    
    @Schema(description = "Last update time")
    public Date lastUpdateTime;
    
    @Schema(description = "Tenant ID", example = "default")
    public String tenantId;
}

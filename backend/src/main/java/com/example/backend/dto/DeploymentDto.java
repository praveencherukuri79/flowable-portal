package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Deployment information with resources and metadata")
public class DeploymentDto {
    
    @Schema(description = "Deployment ID", example = "deploy123")
    public String id;
    
    @Schema(description = "Deployment name", example = "Retention Offer Process v1.0")
    public String name;
    
    @Schema(description = "Deployment timestamp")
    public Date deploymentTime;
    
    @Schema(description = "List of deployed resources")
    public List<String> resources;
    
    @Schema(description = "Tenant ID", example = "default")
    public String tenantId;
}

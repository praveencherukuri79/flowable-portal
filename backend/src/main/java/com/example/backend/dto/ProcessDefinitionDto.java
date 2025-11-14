package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Flowable Process Definition.
 * Contains process template information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDefinitionDto {
    /**
     * Unique identifier for this process definition (e.g., "retentionOfferProcess:1:abc123")
     */
    public String id;

    /**
     * Process definition key (e.g., "retentionOfferProcess", "genericMakerCheckerProcess")
     */
    public String key;

    /**
     * Human-readable name of the process
     */
    public String name;

    /**
     * Version number of this process definition
     */
    public int version;

    /**
     * ID of the deployment this definition belongs to
     */
    public String deploymentId;

    /**
     * Category of the process (optional)
     */
    public String category;

    /**
     * Description of the process (optional)
     */
    public String description;

    /**
     * Whether this process definition is suspended
     */
    public boolean suspended;

    /**
     * Tenant ID (multi-tenancy support)
     */
    public String tenantId;
}

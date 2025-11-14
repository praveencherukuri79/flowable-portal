package com.example.backend.service;

import com.example.backend.dto.ProcessDefinitionDto;

import java.util.List;

/**
 * Service for managing Flowable Process Definitions.
 */
public interface FlowableProcessDefinitionService {
    /**
     * Get all process definitions.
     * @return list of ProcessDefinitionDto
     */
    List<ProcessDefinitionDto> getAllProcessDefinitions();

    /**
     * Get process definition by key.
     * @param key process definition key
     * @return ProcessDefinitionDto or null
     */
    ProcessDefinitionDto getProcessDefinitionByKey(String key);

    /**
     * Get process definition by ID.
     * @param id process definition id
     * @return ProcessDefinitionDto or null
     */
    ProcessDefinitionDto getProcessDefinitionById(String id);

    /**
     * Get latest process definitions (one per key).
     * @return list of latest ProcessDefinitionDto
     */
    List<ProcessDefinitionDto> getLatestProcessDefinitions();
}

package com.example.backend.service.impl;

import com.example.backend.dto.ProcessDefinitionDto;
import com.example.backend.service.FlowableProcessDefinitionService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of Flowable Process Definition Service.
 */
@Service
public class FlowableProcessDefinitionServiceImpl implements FlowableProcessDefinitionService {

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public List<ProcessDefinitionDto> getAllProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionKey().asc()
                .orderByProcessDefinitionVersion().desc()
                .list()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProcessDefinitionDto getProcessDefinitionByKey(String key) {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .latestVersion()
                .singleResult();
        return pd != null ? toDto(pd) : null;
    }

    @Override
    public ProcessDefinitionDto getProcessDefinitionById(String id) {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(id)
                .singleResult();
        return pd != null ? toDto(pd) : null;
    }

    @Override
    public List<ProcessDefinitionDto> getLatestProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionKey().asc()
                .list()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ProcessDefinitionDto toDto(ProcessDefinition pd) {
        return ProcessDefinitionDto.builder()
                .id(pd.getId())
                .key(pd.getKey())
                .name(pd.getName())
                .version(pd.getVersion())
                .deploymentId(pd.getDeploymentId())
                .category(pd.getCategory())
                .description(pd.getDescription())
                .suspended(pd.isSuspended())
                .tenantId(pd.getTenantId())
                .build();
    }
}

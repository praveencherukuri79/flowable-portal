package com.example.backend.service.impl;

import com.example.backend.dto.DeploymentDto;
import com.example.backend.service.FlowableDeploymentService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FlowableDeploymentServiceImpl implements FlowableDeploymentService {

    @Autowired
    private RepositoryService repositoryService;

    @Override
    @Cacheable("deployments")
    public List<DeploymentDto> getAllDeployments() {
        return repositoryService.createDeploymentQuery()
                .list()
                .stream()
                .map(this::toDeploymentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "deployments", key = "#deploymentId")
    public DeploymentDto getDeployment(String deploymentId) {
        Deployment deployment = repositoryService.createDeploymentQuery()
                .deploymentId(deploymentId)
                .singleResult();
        return deployment != null ? toDeploymentDto(deployment) : null;
    }

    @Override
    @CacheEvict(value = {"deployments", "processDefinitions"}, allEntries = true)
    public DeploymentDto deployProcess(MultipartFile file, String name) {
        try {
            Deployment deployment = repositoryService.createDeployment()
                    .name(name != null ? name : file.getOriginalFilename())
                    .addInputStream(file.getOriginalFilename(), file.getInputStream())
                    .deploy();
            return toDeploymentDto(deployment);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deploy process", e);
        }
    }

    @Override
    @CacheEvict(value = {"deployments", "processDefinitions"}, allEntries = true)
    public DeploymentDto deployProcessFromClasspath(String resourcePath, String name) {
        Deployment deployment = repositoryService.createDeployment()
                .name(name)
                .addClasspathResource(resourcePath)
                .deploy();
        return toDeploymentDto(deployment);
    }

    @Override
    @CacheEvict(value = {"deployments", "processDefinitions", "processInstances"}, allEntries = true)
    public void deleteDeployment(String deploymentId, boolean cascade) {
        if (cascade) {
            repositoryService.deleteDeployment(deploymentId, true);
        } else {
            repositoryService.deleteDeployment(deploymentId);
        }
    }

    @Override
    public List<String> getResourceNames(String deploymentId) {
        return repositoryService.getDeploymentResourceNames(deploymentId);
    }

    @Override
    public byte[] getResource(String deploymentId, String resourceName) {
        try {
            return repositoryService.getResourceAsStream(deploymentId, resourceName)
                    .readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource", e);
        }
    }

    @Override
    @Cacheable("deployments")
    public Map<String, Object> getDeploymentStatistics() {
        long totalDeployments = repositoryService.createDeploymentQuery().count();
        long processDefinitions = repositoryService.createProcessDefinitionQuery().count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDeployments", totalDeployments);
        stats.put("processDefinitions", processDefinitions);
        
        return stats;
    }

    private DeploymentDto toDeploymentDto(Deployment deployment) {
        DeploymentDto dto = new DeploymentDto();
        dto.id = deployment.getId();
        dto.name = deployment.getName();
        dto.deploymentTime = deployment.getDeploymentTime();
        dto.tenantId = deployment.getTenantId();
        dto.resources = repositoryService.getDeploymentResourceNames(deployment.getId());
        return dto;
    }
}
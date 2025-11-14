package com.example.backend.service.impl;

import com.example.backend.service.FlowableDiagramService;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class FlowableDiagramServiceImpl implements FlowableDiagramService {
    
    @Autowired
    private RepositoryService repositoryService;

    @Override
    @Cacheable(value = "processDefinitions", key = "#processDefinitionKey")
    public BpmnModel getBpmnModel(String processDefinitionKey) {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();
        
        if (pd == null) {
            throw new RuntimeException("Process definition not found: " + processDefinitionKey);
        }
        
        return repositoryService.getBpmnModel(pd.getId());
    }

    @Override
    @Cacheable(value = "processDefinitions", key = "#processDefinitionKey + '_xml'")
    public String getBpmnXml(String processDefinitionKey) {
        try {
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(processDefinitionKey)
                    .latestVersion()
                    .singleResult();
            
            if (pd == null) {
                throw new RuntimeException("Process definition not found: " + processDefinitionKey);
            }
            
            // Get the BPMN resource directly from the repository
            byte[] resourceBytes = repositoryService.getResourceAsStream(
                    pd.getDeploymentId(), 
                    pd.getResourceName()
            ).readAllBytes();
            
            return new String(resourceBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get BPMN XML for process: " + processDefinitionKey, e);
        }
    }

    @Override
    @Cacheable(value = "processDefinitions", key = "#processDefinitionId + '_diagram'")
    public byte[] getProcessDiagram(String processDefinitionId) {
        try {
            return repositoryService.getProcessDiagram(processDefinitionId).readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get process diagram", e);
        }
    }

    @Override
    @Cacheable(value = "processDefinitions", key = "#processDefinitionId + '_has_diagram'")
    public boolean hasProcessDiagram(String processDefinitionId) {
        try {
            return repositoryService.getProcessDiagram(processDefinitionId) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
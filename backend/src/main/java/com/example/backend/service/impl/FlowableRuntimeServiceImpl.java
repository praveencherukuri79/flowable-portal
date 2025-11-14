package com.example.backend.service.impl;

import com.example.backend.service.FlowableRuntimeService;
import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FlowableRuntimeServiceImpl implements FlowableRuntimeService {
    
    @Autowired
    private RuntimeService runtimeService;

    @Override
    @CacheEvict(value = "processInstances", allEntries = true)
    public String startProcess(String processKey) {
        return runtimeService.startProcessInstanceByKey(processKey).getId();
    }

    @Override
    @CacheEvict(value = "processInstances", allEntries = true)
    public String startProcessWithVariables(String processKey, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKey(processKey, variables).getId();
    }

    @Override
    @CacheEvict(value = "processInstances", allEntries = true)
    public void suspendProcessInstance(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    @Override
    @CacheEvict(value = "processInstances", allEntries = true)
    public void activateProcessInstance(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    @Override
    @CacheEvict(value = "processInstances", allEntries = true)
    public void deleteProcessInstance(String processInstanceId, String deleteReason) {
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
    }

    @Override
    public void setVariable(String processInstanceId, String variableName, Object value) {
        runtimeService.setVariable(processInstanceId, variableName, value);
    }

    @Override
    public Object getVariable(String processInstanceId, String variableName) {
        return runtimeService.getVariable(processInstanceId, variableName);
    }
}
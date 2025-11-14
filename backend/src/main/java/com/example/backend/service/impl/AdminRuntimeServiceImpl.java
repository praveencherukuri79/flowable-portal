package com.example.backend.service.impl;

import com.example.backend.dto.*;
import com.example.backend.service.AdminRuntimeService;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminRuntimeServiceImpl implements AdminRuntimeService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managementService;

    @Override
    public List<ProcessDefinitionDto> getProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .list()
                .stream()
                .map(this::toProcessDefinitionDto)
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponse<ProcessInstanceDto> searchProcessInstances(String definitionKey, String state, int page, int size) {
        var query = historyService.createHistoricProcessInstanceQuery();

        if (definitionKey != null && !definitionKey.isBlank()) {
            query.processDefinitionKey(definitionKey);
        }
        if ("RUNNING".equalsIgnoreCase(state)) {
            query.unfinished();
        }
        if ("COMPLETED".equalsIgnoreCase(state)) {
            query.finished();
        }

        long total = query.count();
        List<HistoricProcessInstance> results = query.orderByProcessInstanceStartTime().desc().listPage(page * size, size);

        List<ProcessInstanceDto> content = results.stream()
                .map(pi -> toProcessInstanceDto(pi, getVariables(pi.getId())))
                .collect(Collectors.toList());

        PagedResponse<ProcessInstanceDto> response = new PagedResponse<>();
        response.content = content;
        response.total = total;
        return response;
    }

    @Override
    public List<EventLogDto> getEventLogs(int limit) {
        try {
            return managementService.getEventLogEntries(0L, (long) limit)
                    .stream()
                    .map(this::toEventLogDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Event log might not be enabled, return empty list
            return new ArrayList<>();
        }
    }

    @Override
    public String generateProcessDiagramSvg(String processInstanceId) {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (pi == null) {
            throw new RuntimeException("Process instance not found: " + processInstanceId);
        }

        try {
            InputStream svgStream = managementService.executeCommand(
                    new org.flowable.engine.impl.cmd.GetDeploymentProcessDiagramCmd(pi.getProcessDefinitionId())
            );
            if (svgStream != null) {
                byte[] svg = svgStream.readAllBytes();
                svgStream.close();
                return new String(svg, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading diagram", e);
        }
        return "";
    }

    private Map<String, Object> getVariables(String processInstanceId) {
        List<?> variables = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        
        Map<String, Object> result = new HashMap<>();
        for (Object var : variables) {
            try {
                java.lang.reflect.Method getVariableName = var.getClass().getMethod("getVariableName");
                java.lang.reflect.Method getValue = var.getClass().getMethod("getValue");
                String name = (String) getVariableName.invoke(var);
                Object value = getValue.invoke(var);
                result.put(name, value);
            } catch (Exception e) {
                // Skip if reflection fails
            }
        }
        return result;
    }

    private ProcessDefinitionDto toProcessDefinitionDto(ProcessDefinition d) {
        ProcessDefinitionDto dto = new ProcessDefinitionDto();
        dto.id = d.getId();
        dto.key = d.getKey();
        dto.name = d.getName();
        dto.category = d.getCategory();
        dto.description = d.getDescription();
        dto.version = d.getVersion();
        dto.deploymentId = d.getDeploymentId();
        dto.suspended = d.isSuspended();
        dto.tenantId = d.getTenantId();
        return dto;
    }

    private ProcessInstanceDto toProcessInstanceDto(HistoricProcessInstance pi, Map<String, Object> vars) {
        ProcessInstanceDto dto = new ProcessInstanceDto();
        dto.id = pi.getId();
        dto.processDefinitionId = pi.getProcessDefinitionId();
        dto.processDefinitionKey = pi.getProcessDefinitionKey();
        dto.businessKey = pi.getBusinessKey();
        dto.startUserId = pi.getStartUserId();
        dto.startTime = pi.getStartTime() != null ? pi.getStartTime().toString() : null;
        dto.endTime = pi.getEndTime() != null ? pi.getEndTime().toString() : null;
        dto.status = pi.getEndTime() == null ? "RUNNING" : "COMPLETED";
        dto.variables = vars;
        dto.tenantId = pi.getTenantId();
        dto.suspended = false;
        dto.name = pi.getName();
        dto.description = pi.getDescription();
        return dto;
    }

    private EventLogDto toEventLogDto(Object eventLogEntry) {
        // Use reflection to handle EventLogEntry as it may not be available in all Flowable versions
        try {
            EventLogDto dto = new EventLogDto();
            java.lang.reflect.Method getLogNumber = eventLogEntry.getClass().getMethod("getLogNumber");
            java.lang.reflect.Method getTimeStamp = eventLogEntry.getClass().getMethod("getTimeStamp");
            java.lang.reflect.Method getType = eventLogEntry.getClass().getMethod("getType");
            java.lang.reflect.Method getProcessDefinitionId = eventLogEntry.getClass().getMethod("getProcessDefinitionId");
            java.lang.reflect.Method getProcessInstanceId = eventLogEntry.getClass().getMethod("getProcessInstanceId");
            java.lang.reflect.Method getExecutionId = eventLogEntry.getClass().getMethod("getExecutionId");
            java.lang.reflect.Method getData = eventLogEntry.getClass().getMethod("getData");

            dto.id = String.valueOf(getLogNumber.invoke(eventLogEntry));
            dto.timestamp = (java.util.Date) getTimeStamp.invoke(eventLogEntry);
            dto.type = (String) getType.invoke(eventLogEntry);
            dto.processDefinitionId = (String) getProcessDefinitionId.invoke(eventLogEntry);
            dto.processInstanceId = (String) getProcessInstanceId.invoke(eventLogEntry);
            dto.executionId = (String) getExecutionId.invoke(eventLogEntry);
            byte[] data = (byte[]) getData.invoke(eventLogEntry);
            dto.data = data != null ? new String(data, StandardCharsets.UTF_8) : null;
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping EventLogEntry", e);
        }
    }
}


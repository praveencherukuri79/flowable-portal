package com.example.backend.util;

import com.example.backend.dto.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Centralized static utility class for converting Flowable entities to DTOs.
 * Consolidates all DTO mapping methods from services for better organization and reusability.
 * Uses Lombok builder pattern for clean, immutable DTO creation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DtoMapper {

    // ==================== PROCESS INSTANCE MAPPING ====================
    
    /**
     * Convert ProcessInstance to ProcessInstanceDto using builder pattern
     */
    public static ProcessInstanceDto toProcessInstanceDto(ProcessInstance processInstance) {
        return ProcessInstanceDto.builder()
                .id(processInstance.getId())
                .processDefinitionId(processInstance.getProcessDefinitionId())
                .processDefinitionKey(processInstance.getProcessDefinitionKey())
                .businessKey(processInstance.getBusinessKey())
                .startUserId(processInstance.getStartUserId())
                .startTime(processInstance.getStartTime() != null ? processInstance.getStartTime().toString() : null)
                .suspended(processInstance.isSuspended())
                .tenantId(processInstance.getTenantId())
                .name(processInstance.getName())
                .description(processInstance.getDescription())
                .build();
    }

    // ==================== TASK MAPPING ====================
    
    /**
     * Convert Task to TaskDto using builder pattern
     */
    public static TaskDto toTaskDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .assignee(task.getAssignee())
                .owner(task.getOwner())
                .delegationState(task.getDelegationState() != null ? task.getDelegationState().toString() : null)
                .processInstanceId(task.getProcessInstanceId())
                .processDefinitionId(task.getProcessDefinitionId())
                .executionId(task.getExecutionId())
                .taskDefinitionKey(task.getTaskDefinitionKey())
                .createTime(task.getCreateTime())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .category(task.getCategory())
                .formKey(task.getFormKey())
                .suspended(String.valueOf(task.isSuspended()))
                .tenantId(task.getTenantId())
                .build();
    }

    // ==================== DEPLOYMENT MAPPING ====================
    
    /**
     * Convert Deployment to DeploymentDto using builder pattern
     */
    public static DeploymentDto toDeploymentDto(Deployment deployment) {
        return DeploymentDto.builder()
                .id(deployment.getId())
                .name(deployment.getName())
                .deploymentTime(deployment.getDeploymentTime())
                .tenantId(deployment.getTenantId())
                .build();
    }

    // ==================== MODEL MAPPING ====================
    
    /**
     * Convert Model to ModelDto using builder pattern
     */
    public static ModelDto toModelDto(Model model) {
        return ModelDto.builder()
                .id(model.getId())
                .name(model.getName())
                .key(model.getKey())
                .category(model.getCategory())
                .version(model.getVersion() != null ? model.getVersion().toString() : null)
                .metaInfo(model.getMetaInfo())
                .createTime(model.getCreateTime())
                .lastUpdateTime(model.getLastUpdateTime())
                .tenantId(model.getTenantId())
                .build();
    }

    // ==================== USER & GROUP MAPPING REMOVED ====================
    // Identity service functionality removed as per requirements    // ==================== HISTORY MAPPING ====================
    
    /**
     * Convert HistoricProcessInstance to ProcessInstanceDto (reusing ProcessInstanceDto for historic instances)
     */
    public static ProcessInstanceDto toProcessInstanceDto(HistoricProcessInstance hpi) {
        return ProcessInstanceDto.builder()
                .id(hpi.getId())
                .processDefinitionId(hpi.getProcessDefinitionId())
                .processDefinitionKey(hpi.getProcessDefinitionKey())
                .businessKey(hpi.getBusinessKey())
                .startUserId(hpi.getStartUserId())
                .startTime(hpi.getStartTime() != null ? hpi.getStartTime().toString() : null)
                .endTime(hpi.getEndTime() != null ? hpi.getEndTime().toString() : null)
                .status(hpi.getEndTime() == null ? "RUNNING" : "COMPLETED")
                .tenantId(hpi.getTenantId())
                .suspended(false) // Historic instances are always not suspended
                .name(hpi.getName())
                .description(hpi.getDescription())
                .build();
    }

    /**
     * Convert HistoricTaskInstance to TaskDto (reusing TaskDto for historic tasks)
     */
    public static TaskDto toTaskDto(HistoricTaskInstance hti) {
        return TaskDto.builder()
                .id(hti.getId())
                .name(hti.getName())
                .description(hti.getDescription())
                .assignee(hti.getAssignee())
                .owner(hti.getOwner())
                .processInstanceId(hti.getProcessInstanceId())
                .processDefinitionId(hti.getProcessDefinitionId())
                .executionId(hti.getExecutionId())
                .taskDefinitionKey(hti.getTaskDefinitionKey())
                .createTime(hti.getCreateTime())
                .endTime(hti.getEndTime())
                .durationInMillis(hti.getDurationInMillis())
                .priority(hti.getPriority())
                .category(hti.getCategory())
                .formKey(hti.getFormKey())
                .tenantId(hti.getTenantId())
                .suspended("false") // Historic tasks are always not suspended
                .state(hti.getEndTime() != null ? "COMPLETED" : "ACTIVE")
                .build();
    }

    // ==================== ENGINE INFO MAPPING ====================
    
    /**
     * Convert engine information to EngineInfoDto using builder pattern
     */
    public static EngineInfoDto toEngineInfoDto(String name, String version, String resourceUrl, String exception) {
        return EngineInfoDto.builder()
                .name(name)
                .version(version)
                .resourceUrl(resourceUrl)
                .exception(exception)
                .build();
    }

    // ==================== ADMIN PORTAL MAPPINGS ====================
    
    /**
     * Convert ProcessDefinition to ProcessDefinitionDto for admin portal
     */
    public static ProcessDefinitionDto toProcessDefinitionDto(ProcessDefinition d) {
        return ProcessDefinitionDto.builder()
                .id(d.getId())
                .key(d.getKey())
                .name(d.getName())
                .category(d.getCategory())
                .description(d.getDescription())
                .version(d.getVersion())
                .deploymentId(d.getDeploymentId())
                .suspended(d.isSuspended())
                .tenantId(d.getTenantId())
                .build();
    }
    
    /**
     * Convert HistoricProcessInstance to ProcessInstanceDto for admin portal (with variables)
     * Note: This method is kept for backward compatibility, but now uses the consolidated ProcessInstanceDto
     */
    public static ProcessInstanceDto toProcessInstanceDto(HistoricProcessInstance pi, Map<String, Object> vars) {
        ProcessInstanceDto dto = toProcessInstanceDto(pi);
        dto.setVariables(vars);
        return dto;
    }
    
    /**
     * Convert Task to TaskDto with variables for admin portal
     */
    public static TaskDto toTaskDto(Task t, Map<String, Object> vars) {
        TaskDto dto = toTaskDto(t);
        dto.setVariables(vars);
        dto.setState(t.getAssignee() == null ? "CLAIMABLE" : "ASSIGNED");
        return dto;
    }
    
    /**
     * Convert EventLogEntry to EventLogDto for admin portal
     * Uses reflection to handle EventLogEntry as it may not be available in all Flowable versions
     */
    public static EventLogDto toEventLogDto(Object eventLogEntry) {
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
    
    // ==================== OTHER MAPPINGS ====================
    
    // TODO: Add Comment and Attachment mappings when needed
    // Note: Comment and Attachment classes may not be directly available in this Flowable version
}
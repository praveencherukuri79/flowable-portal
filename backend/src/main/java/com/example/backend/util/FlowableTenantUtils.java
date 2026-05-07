package com.example.backend.util;

import com.example.backend.tenant.TenantContextHolder;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfoQuery;

/**
 * Standalone examples of the tenant-aware helper methods used across the backend.
 *
 * This class is intentionally not wired into the application yet.
 * It exists only as a readable reference for the common patterns:
 * - get the current request tenant once
 * - build tenant-scoped Flowable queries
 * - fail fast when a task or process instance is not found for that tenant
 */
public final class FlowableTenantUtils {

    private FlowableTenantUtils() {
    }

    public static String currentTenantId() {
        return TenantContextHolder.getRequiredTenantId();
    }

    public static TaskInfoQuery<?, ?> taskQueryForCurrentTenant(TaskService taskService) {
        return taskService.createTaskQuery()
                .taskTenantId(currentTenantId());
    }

    public static Task requireTaskForCurrentTenant(TaskService taskService, String taskId) {
        Task task = taskService.createTaskQuery()
                .taskTenantId(currentTenantId())
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new FlowableObjectNotFoundException(
                    "Task not found for tenant: " + taskId,
                    Task.class
            );
        }

        return task;
    }

    public static ProcessInstanceQuery processInstanceQueryForCurrentTenant(RuntimeService runtimeService) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceTenantId(currentTenantId());
    }

    public static ProcessInstance requireProcessInstanceForCurrentTenant(
            RuntimeService runtimeService,
            String processInstanceId
    ) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceTenantId(currentTenantId())
                .processInstanceId(processInstanceId)
                .singleResult();

        if (processInstance == null) {
            throw new FlowableObjectNotFoundException(
                    "Process instance not found for tenant: " + processInstanceId,
                    ProcessInstance.class
            );
        }

        return processInstance;
    }

    public static HistoricProcessInstanceQuery historicProcessInstanceQueryForCurrentTenant(
            HistoryService historyService
    ) {
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceTenantId(currentTenantId());
    }

    public static HistoricProcessInstance requireHistoricProcessInstanceForCurrentTenant(
            HistoryService historyService,
            String processInstanceId
    ) {
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceTenantId(currentTenantId())
                .processInstanceId(processInstanceId)
                .singleResult();

        if (historicProcessInstance == null) {
            throw new FlowableObjectNotFoundException(
                    "Historic process instance not found for tenant: " + processInstanceId,
                    HistoricProcessInstance.class
            );
        }

        return historicProcessInstance;
    }
}
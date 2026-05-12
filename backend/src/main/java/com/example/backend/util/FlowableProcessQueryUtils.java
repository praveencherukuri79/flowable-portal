package com.example.backend.util;

import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import java.util.List;

/**
 * Query helpers focused on process-definition-key based lookups.
 *
 * This utility is intentionally tenant-agnostic.
 * Add tenant filtering at the call site when needed.
 */
public final class FlowableProcessQueryUtils {

        private static final int SINGLE_PROCESS_QUERY_LIMIT = 2;

    private FlowableProcessQueryUtils() {
    }

    public static List<ProcessInstance> getActiveProcessesByDefinitionKey(
            RuntimeService runtimeService,
            String processDefinitionKey
    ) {
        return runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .active()
                .list();
    }

    public static boolean hasActiveProcessByDefinitionKey(
            RuntimeService runtimeService,
            String processDefinitionKey
    ) {
        return runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .active()
                .count() > 0;
    }

    public static ProcessInstance getSingleActiveProcessByDefinitionKey(
            RuntimeService runtimeService,
            String processDefinitionKey
    ) {
        List<ProcessInstance> activeProcesses = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .active()
                .listPage(0, SINGLE_PROCESS_QUERY_LIMIT);

        if (activeProcesses.isEmpty()) {
            throw new FlowableObjectNotFoundException(
                    "No active process found for definition key: " + processDefinitionKey,
                    ProcessInstance.class
            );
        }

        if (activeProcesses.size() > 1) {
            throw new IllegalStateException(
                    "Expected one active process for definition key but found multiple: " + processDefinitionKey
            );
        }

        return activeProcesses.get(0);
    }

    public static List<HistoricProcessInstance> getCompletedProcessesByDefinitionKey(
            HistoryService historyService,
            String processDefinitionKey
    ) {
        return historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .finished()
                .notDeleted()
                .list();
    }

    public static boolean hasCompletedProcessByDefinitionKey(
            HistoryService historyService,
            String processDefinitionKey
    ) {
        return historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .finished()
                .notDeleted()
                .count() > 0;
    }

    public static List<Task> getActiveTasksForActiveProcessesByDefinitionKey(
            RuntimeService runtimeService,
            TaskService taskService,
            String processDefinitionKey
    ) {
        List<String> activeProcessInstanceIds = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .active()
                .list()
                .stream()
                .map(ProcessInstance::getId)
                .toList();

        if (activeProcessInstanceIds.isEmpty()) {
            return List.of();
        }

        return taskService.createTaskQuery()
                .processInstanceIdIn(activeProcessInstanceIds)
                .active()
                .list();
    }

    public static Task getFirstActiveTaskForActiveProcessByDefinitionKey(
            RuntimeService runtimeService,
            TaskService taskService,
            String processDefinitionKey
    ) {
        ProcessInstance activeProcess = getSingleActiveProcessByDefinitionKey(
                runtimeService,
                processDefinitionKey
        );

        List<Task> activeTasks = taskService.createTaskQuery()
                .processInstanceId(activeProcess.getId())
                .active()
                .list();

        if (activeTasks.isEmpty()) {
            throw new FlowableObjectNotFoundException(
                    "No active task found for active process definition key: " + processDefinitionKey,
                    Task.class
            );
        }

        return activeTasks.get(0);
    }
}
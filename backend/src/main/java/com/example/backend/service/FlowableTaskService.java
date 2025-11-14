package com.example.backend.service;

import com.example.backend.dto.TaskDto;
import java.util.List;

public interface FlowableTaskService {
    List<TaskDto> getTasksForUser(String user);
    void claimTask(String taskId, String user);
    void completeTask(String taskId, java.util.Map<String, Object> variables);
    void reassignTask(String taskId, String newUser);
    void delegateTask(String taskId, String delegateUser);
}

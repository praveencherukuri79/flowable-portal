package com.example.backend.service;

import com.example.backend.dto.PagedResponse;
import com.example.backend.dto.TaskDto;

public interface AdminTaskService {
    PagedResponse<TaskDto> searchTasks(String candidateGroup, String state, int page, int size);
}


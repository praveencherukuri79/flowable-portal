package com.example.backend.service;

import com.example.backend.dto.EventLogDto;
import com.example.backend.dto.PagedResponse;
import com.example.backend.dto.ProcessDefinitionDto;
import com.example.backend.dto.ProcessInstanceDto;

import java.util.List;

public interface AdminRuntimeService {
    List<ProcessDefinitionDto> getProcessDefinitions();
    PagedResponse<ProcessInstanceDto> searchProcessInstances(String definitionKey, String state, int page, int size);
    List<EventLogDto> getEventLogs(int limit);
    String generateProcessDiagramSvg(String processInstanceId);
}


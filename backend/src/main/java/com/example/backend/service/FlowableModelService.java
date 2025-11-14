package com.example.backend.service;

import com.example.backend.dto.ModelDto;
import java.util.List;
import java.util.Map;

public interface FlowableModelService {
    List<ModelDto> getAllModels();
    ModelDto getModel(String modelId);
    ModelDto createModel(String name, String key, String description);
    ModelDto updateModel(String modelId, String name, String key, String description);
    void deleteModel(String modelId);
    byte[] getModelEditorSource(String modelId);
    void saveModelEditorSource(String modelId, byte[] editorSource);
    Map<String, Object> getModelStatistics();
}
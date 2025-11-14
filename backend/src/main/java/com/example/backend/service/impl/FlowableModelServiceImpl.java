package com.example.backend.service.impl;

import com.example.backend.dto.ModelDto;
import com.example.backend.service.FlowableModelService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FlowableModelServiceImpl implements FlowableModelService {

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public List<ModelDto> getAllModels() {
        return repositoryService.createModelQuery()
                .list()
                .stream()
                .map(this::toModelDto)
                .collect(Collectors.toList());
    }

    @Override
    public ModelDto getModel(String modelId) {
        Model model = repositoryService.getModel(modelId);
        return model != null ? toModelDto(model) : null;
    }

    @Override
    public ModelDto createModel(String name, String key, String description) {
        Model model = repositoryService.newModel();
        model.setName(name);
        model.setKey(key);
        model.setMetaInfo(description);
        repositoryService.saveModel(model);
        return toModelDto(model);
    }

    @Override
    public ModelDto updateModel(String modelId, String name, String key, String description) {
        Model model = repositoryService.getModel(modelId);
        if (model != null) {
            model.setName(name);
            model.setKey(key);
            model.setMetaInfo(description);
            repositoryService.saveModel(model);
            return toModelDto(model);
        }
        return null;
    }

    @Override
    public void deleteModel(String modelId) {
        repositoryService.deleteModel(modelId);
    }

    @Override
    public byte[] getModelEditorSource(String modelId) {
        return repositoryService.getModelEditorSource(modelId);
    }

    @Override
    public void saveModelEditorSource(String modelId, byte[] editorSource) {
        repositoryService.addModelEditorSource(modelId, editorSource);
    }

    @Override
    public Map<String, Object> getModelStatistics() {
        long totalModels = repositoryService.createModelQuery().count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalModels", totalModels);
        
        return stats;
    }

    private ModelDto toModelDto(Model model) {
        ModelDto dto = new ModelDto();
        dto.id = model.getId();
        dto.name = model.getName();
        dto.key = model.getKey();
        dto.category = model.getCategory();
        dto.version = model.getVersion() != null ? String.valueOf(model.getVersion()) : null;
        dto.metaInfo = model.getMetaInfo();
        dto.createTime = model.getCreateTime();
        dto.lastUpdateTime = model.getLastUpdateTime();
        dto.tenantId = model.getTenantId();
        return dto;
    }
}
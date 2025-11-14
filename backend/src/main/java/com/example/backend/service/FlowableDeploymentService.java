package com.example.backend.service;

import com.example.backend.dto.DeploymentDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface FlowableDeploymentService {
    List<DeploymentDto> getAllDeployments();
    DeploymentDto getDeployment(String deploymentId);
    DeploymentDto deployProcess(MultipartFile file, String name);
    DeploymentDto deployProcessFromClasspath(String resourcePath, String name);
    void deleteDeployment(String deploymentId, boolean cascade);
    List<String> getResourceNames(String deploymentId);
    byte[] getResource(String deploymentId, String resourceName);
    Map<String, Object> getDeploymentStatistics();
}
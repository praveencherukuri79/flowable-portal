package com.example.backend.service;

import org.flowable.bpmn.model.BpmnModel;

/**
 * Service interface for BPMN diagram operations.
 */
public interface FlowableDiagramService {
    
    /**
     * Get BPMN model for process definition.
     * @param processDefinitionKey process definition key
     * @return BpmnModel object
     */
    BpmnModel getBpmnModel(String processDefinitionKey);
    
    /**
     * Get BPMN XML for process definition.
     * @param processDefinitionKey process definition key
     * @return BPMN XML string
     */
    String getBpmnXml(String processDefinitionKey);
    
    /**
     * Get process diagram as byte array.
     * @param processDefinitionId process definition id
     * @return diagram image bytes
     */
    byte[] getProcessDiagram(String processDefinitionId);
    
    /**
     * Check if process definition has diagram.
     * @param processDefinitionId process definition id
     * @return true if diagram exists
     */
    boolean hasProcessDiagram(String processDefinitionId);
}

package com.example.backend.controller;

import com.example.backend.service.FlowableDiagramService;
import org.flowable.bpmn.model.BpmnModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flowable/diagram")
@CrossOrigin(origins = "http://localhost:3002", maxAge = 3600, allowCredentials = "true")
public class FlowableDiagramController {
    @Autowired
    private FlowableDiagramService diagramService;

    /**
     * Get BPMN model object for process definition.
     * @param processDefinitionKey process definition key
     * @return BpmnModel object (JSON)
     */
    @GetMapping("/{processDefinitionKey}")
    public BpmnModel getBpmnModel(@PathVariable String processDefinitionKey) {
        return diagramService.getBpmnModel(processDefinitionKey);
    }

    /**
     * Get BPMN XML for process definition.
     * @param processDefinitionKey process definition key
     * @return BPMN XML string
     */
    @GetMapping(value = "/{processDefinitionKey}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getBpmnXml(@PathVariable String processDefinitionKey) {
        String xml = diagramService.getBpmnXml(processDefinitionKey);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }
}

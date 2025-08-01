package io.sendur.service.impl;

import io.sendur.domain.workflow.Workflow;
import io.sendur.repository.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowService.class);

    private final WorkflowRepository workflowRepository;

    @Autowired
    public WorkflowService(final WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    public Workflow saveWorkflow(Workflow workflow) {
        return workflowRepository.save(workflow);
    }

    public List<Workflow> findAllWorkflows() {
        return workflowRepository.findAll();
    }
}

package io.sendur.service.impl;

import io.sendur.domain.execution.ExecutionsResult;
import io.sendur.repository.ExecutionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ExecutionsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionsService.class);

    private final ExecutionsRepository executionsRepository;
    private final N8NGatewayService n8NGatewayService;

    public ExecutionsService(ExecutionsRepository executionsRepository, N8NGatewayService n8NGatewayService) {
        this.executionsRepository = executionsRepository;
        this.n8NGatewayService = n8NGatewayService;
    }

    public ExecutionsResult getExecutionsByWorkflowId(String workflowId) {
        // ideally we want to add tracing here before sending to controller
        return n8NGatewayService.retrieveExecutionsByWorkflowId(workflowId);
    }

    public ExecutionsResult getExecutionByExecutionId(String executionId) {
        return n8NGatewayService.retrieveExecutionByExecutionId(executionId);
    }

    public ExecutionsResult getExecutionsByExecutionIds(Set<String> executionsIds) {
        return n8NGatewayService.retrieveExecutionsByExecutionsIds(executionsIds);
    }

    public ExecutionsResult getAllExecutions() {
        return n8NGatewayService.retrieveAllExecutions();
    }
}

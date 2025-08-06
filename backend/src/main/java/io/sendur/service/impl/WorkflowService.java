package io.sendur.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sendur.component.cache.WorkflowSessionCache;
import io.sendur.domain.workflow.Workflow;
import io.sendur.repository.WorkflowRepository;
import io.sendur.utils.ReflectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WorkflowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowService.class);

    private final WorkflowRepository workflowRepository;
    private final WorkflowSessionCache workflowSessionCache;

    public static final String AI_AGENT_RAW = "AI Agent - ";

    @Autowired
    public WorkflowService(final WorkflowRepository workflowRepository, WorkflowSessionCache workflowSessionCache) {
        this.workflowRepository = workflowRepository;
        this.workflowSessionCache = workflowSessionCache;
    }

    // TODO: this needs to handle "upserts" - with possibly a boolean upsert param
    public Workflow saveWorkflow(Workflow workflow) {
        Optional<Workflow> existingWorkflow = workflowRepository.findByWorkflowId(workflow.getWorkflowId());
        if (existingWorkflow.isPresent()) {
            LOGGER.warn("Workflow with workflowId already exists {}", workflow.getWorkflowId());
            return existingWorkflow.get();
        }
        Workflow savedWorkflow = workflowRepository.save(workflow);
        workflowSessionCache.add(savedWorkflow);
        return savedWorkflow;
    }

    /**
     * Workflow refresh bypasses cache and pulls the most upto date workflows from
     * data storage and caches these workflows.
     */
    public void refresh() {
        List<Workflow> allWorkflows = workflowRepository.findAll();
        workflowSessionCache.set(allWorkflows);
    }

    /**
     * Get all workflow names in its Raw form. No truncation is currently processed on these
     * workflow names and what is retrieved is the full workflow name as it was saved and
     * appears in the datastore.
     *
     * @return {@link List<String>} all workflow names
     */
    public List<String> getAllWorkFlowNames() {
        List<Workflow> workflows = findAllWorkflows();
        if (ObjectUtils.isNotEmpty(workflows)) {
            return workflows.stream()
                    .map(Workflow::getName)
                    .filter(StringUtils::isNotBlank)
                    .toList();
        }
        LOGGER.warn("Warning: workflows not available in cache or database. Validate this is intended.");
        return new ArrayList<>();
    }

    /**
     * Get all possible workflows. utilizes the {@link WorkflowSessionCache} and
     * returns all cached workflows. If workflows are not cached, then pulls all
     * workflows from storage.
     *
     * @see #refresh()
     *
     * @return {@link List<Workflow>} list of all workflows
     */
    public List<Workflow> findAllWorkflows() {
        if (workflowSessionCache.isCached()) {
            return workflowSessionCache.get();
        }
        List<Workflow> allWorkflows = workflowRepository.findAll();

        // This can be a false positive - don't set session key with empty object.
        // If called with empty value it can return false empty value. Instead, when
        // cache is checked it should return false and force a pull from datastore.
        if (ObjectUtils.isNotEmpty(allWorkflows)) {
            workflowSessionCache.set(allWorkflows);
        }
        return allWorkflows;
    }

    /**
     * Get all workflow names that are prefixed with {@code AI Agent - } which is the
     * current standard for AI workflows. Workflow names are truncated to only return
     * the base name, removing the prefix.
     *
     * @return {@link List<String>} Names of all workflows that contain AI Agents
     */
    public List<String> getAiAgentWorkFlowNames() {
        List<String> aiWorkflowNames = new ArrayList<>();
        List<String> allWorkflowNames = getAllWorkFlowNames();
        for (String workflowName : allWorkflowNames) {
            if (StringUtils.startsWithIgnoreCase(workflowName, AI_AGENT_RAW)) {
                workflowName = StringUtils.substring(workflowName, AI_AGENT_RAW.length());
                aiWorkflowNames.add(workflowName);
            }
        }
        return aiWorkflowNames;
    }

    /**
     * Searches available workflows and loads valid {@code Workflow} json,
     * given the workflow name.
     *
     * @param workFlowName {@link String} workflow name
     *
     * @return {@link String} workflow json string
     */
    public String loadWorkflowJson(String workFlowName) {
        List<Workflow> allWorkflows = findAllWorkflows();
        Workflow searchedWorkflow = null;
        for (Workflow workflow : allWorkflows) {
            if (StringUtils.containsIgnoreCase(workflow.getName(), workFlowName)) {
                searchedWorkflow = workflow;
                break;
            }
        }
        if (ObjectUtils.isEmpty(searchedWorkflow)) {
            LOGGER.warn("Error finding workflow '{}' in available workflows", workFlowName);
            return StringUtils.EMPTY;
        }
        return toJsonString(searchedWorkflow);
    }

    /**
     * Utilize {@link ObjectMapper} to convert a {@code Workflow} Object to a valid json
     * string. Workflow json string will not contain the database _id PK. However, valid
     * workflows will contain the workflow business id.
     *
     * @param workflow {@link Workflow}
     *
     * @return {@link String} workflow json string or empty json '{}'
     */
    public String toJsonString(Workflow workflow) {
        if (workflow == null || ReflectionUtils.isAllFieldsNull(workflow)) {
            LOGGER.warn("Warning: cannot map empty workflow to json string");
            return StringUtils.defaultString("{}");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String workflowJson = StringUtils.defaultString("{}");
        try {
            workflowJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(workflow);
            return workflowJson;
        } catch (JsonProcessingException e) {
            LOGGER.error("Error writing workflow '{}' as json string: {}",
                    workflow.getName(), e.getMessage());
        }
        return workflowJson;
    }
}

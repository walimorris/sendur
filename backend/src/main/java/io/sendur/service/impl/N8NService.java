package io.sendur.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.sendur.domain.lead.ApprovedLeadsWebhookResult;
import io.sendur.domain.lead.Lead;
import io.sendur.domain.workflow.Workflow;
import io.sendur.domain.workflow.WorkflowConverter;
import io.sendur.repository.LeadRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class N8NService {
    private static final Logger LOGGER = LoggerFactory.getLogger(N8NService.class);

    // n8n parameter types - make this an ENUM
    private static final String LLM_NODE = "@n8n/n8n-nodes-langchain.agent";

    private final N8NGatewayService n8nGatewayService;
    private final LeadRepository leadRepository;
    private final WorkflowService workflowService;

    @Autowired
    public N8NService(final N8NGatewayService n8nGatewayService,
                      final LeadRepository leadRepository,
                      final WorkflowService workflowService) {
        this.n8nGatewayService = n8nGatewayService;
        this.leadRepository = leadRepository;
        this.workflowService = workflowService;
    }

    public ApprovedLeadsWebhookResult sendEmailsToLeads(List<Lead> leads) {
        return n8nGatewayService.sendApprovedEmailsToLeads(this.leadRepository, leads);
    }

    /**
     * Get a workflow node from a workflow's name and the node id of the searched workflow node.
     *
     * @param workflowName {@link String} workflow name
     * @param nodeId {@link String} workflow nodeId
     *
     * @return {@link Map} searched workflow node
     */
    public Map<String, Object> getWorkflowNodeFromNameAndNodeId(String workflowName, String nodeId) {
        if (StringUtils.isNotEmpty(workflowName) && StringUtils.isNotEmpty(nodeId)) {
            Workflow searchedWorkflow = loadWorkflow(workflowName);
            if (ObjectUtils.isEmpty(searchedWorkflow)) {
                return null;
            }
            List<Map<String, Object>> nodes = getWorkflowNodes(searchedWorkflow);
            if (ObjectUtils.isNotEmpty(nodes)) {
                for (Map<String, Object> node : nodes) {
                    String currentNodeId = (String) node.getOrDefault("id", "");
                    if (StringUtils.equals(currentNodeId, nodeId)) {
                        return node;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get a workflow's node given the search node id.
     *
     * @param nodeId {@link String} searched node id
     *
     * @return {@link Map} search workflow node
     */
    public Map<String, Object> getWorkFlowNodeFromNodeId(String nodeId) {
        if (ObjectUtils.isNotEmpty(nodeId)) {
            List<Workflow> workflows = loadAllWorkflows();
            for (Workflow workflow : workflows) {
                List<Map<String, Object>> currentNodes = getWorkflowNodes(workflow);
                for (Map<String, Object> node : currentNodes) {
                    String currentNodeId = (String) node.getOrDefault("id", "");
                    if (StringUtils.equals(currentNodeId, nodeId)) {
                        return node;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get All workflow names that contain AI Agents.
     *
     * @return {@link List<String>} workflow names
     */
    public List<String> getWorkflowNamesWithAiAgents() {
        List<String> workflowNameResults = new ArrayList<>();
        List<String> workFlowWithAiAgentsNames = workflowService.getAiAgentWorkFlowNames();
        for (String workflowName : workFlowWithAiAgentsNames) {
            Workflow currentWorkflow = loadWorkflow(workflowName);
            List<Map<String, Object>> workflowNodes = getWorkflowNodes(currentWorkflow);
            for (Map<String, Object> node : workflowNodes) {
                if (isLlmBearingNode(node)) {
                    workflowNameResults.add(workflowName);
                }
            }
        }
        return workflowNameResults;
    }

    /**
     * Get all Workflow nodes from the given {@link Workflow}.
     *
     * @param workflow {@link Workflow}
     *
     * @return {@link List<Map>} list of all workflow nodes
     */
    public List<Map<String, Object>> getWorkflowNodes(Workflow workflow) {
        return workflow.getNodes();
    }

    /**
     * Retrieve all current LLM prompts from a current workflow. Iterates on the workflow nodes,
     * finds all the agents and pulls the prompt from the agent. Adds the node ID to the Map as
     * a reference point.
     *
     * @param workflowName n8n workflow name to search
     *
     * @return {@link Map} containing Node ID and Prompt
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getLlmPromptsFromWorkflow(String workflowName) {
        if (StringUtils.isNotEmpty(workflowName)) {
            // iterate nodes and get all the llm nodes
            Workflow workflow = loadWorkflow(workflowName);
            if (ObjectUtils.isNotEmpty(workflow)) {
                List<Map<String, Object>> nodes = workflow.getNodes();
                Map<String, String> prompts = new HashMap<>();
                for (Map<String, Object> node : nodes) {
                    String currentNodeId = (String) node.getOrDefault("id", "");
                    if (isLlmBearingNode(node)) {
                        // get the parameter that contain the prompt
                        Map<String, Object> nodeParameters = (Map<String, Object>) node.getOrDefault("parameters", new HashMap<>());
                        String promptText = (String) nodeParameters.getOrDefault("text", "");
                        if (StringUtils.isEmpty(promptText)) {
                            prompts.put(currentNodeId, "empty prompt - review configuration");
                        } else {
                            prompts.put(currentNodeId, promptText);
                        }
                    }
                }
                return prompts;
            }
        }
        return new HashMap<>();
    }

    /**
     * Load n8n workflow configuration from resource path.
     *
     * @param workflowName n8n workflow name
     *
     * @return {@link Workflow} n8n workflow
     */
    public Workflow loadWorkflow(String workflowName) {
        if (StringUtils.isEmpty(workflowName)) {
            return null;
        }
        try {
            final String workflowJson = workflowService.loadWorkflowJson(workflowName);
            if (StringUtils.isNotEmpty(workflowJson) && !workflowJson.trim().contains("{}")) {
                return WorkflowConverter.fromJsonString(workflowJson);
            }
            return null;
        } catch (IOException e) {
            LOGGER.error("Error loading workflow {}: {}", workflowName, e.getMessage());
            return null;
        }
    }

    public boolean saveWorkflow(Workflow workflow) throws JsonProcessingException {
        String workflowJson = WorkflowConverter.toJsonString(workflow);
        LOGGER.info("Saving workflow {}: {}", workflow.getName(), workflowJson);
        return true;
    }

    /**
     * Remove node from workflow.
     *
     * @param workflow {@link Workflow} given workflow
     * @param nodeId {@link UUID} node id
     *
     * @return boolean
     */
    public boolean removeWorkflowNode(Workflow workflow, String nodeId) {
        if (ObjectUtils.isNotEmpty(workflow) && StringUtils.isNotEmpty(nodeId)) {
            for (Map<String, Object> node : workflow.getNodes()) {
                String id = (String) node.getOrDefault("id", "");
                if (StringUtils.equals(id, nodeId)) {
                    workflow.getNodes().remove(node);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Given a node, this method validates if it is an AI agent bearing node.
     *
     * @param node Workflow node
     *
     * @return boolean - if this is an AI agent bearing node.
     */
    private boolean isLlmBearingNode(Map<String, Object> node) {
        String nodeType = (String) node.getOrDefault("type", "");
        return StringUtils.equals(nodeType, LLM_NODE);
    }

    /**
     * Load all possible n8n workflows.
     *
     * @return {@link List<Workflow>} list of workflows
     */
    private List<Workflow> loadAllWorkflows() {
        List<Workflow> workflows = new ArrayList<>();
        List<String> workflowNamesList = workflowService.getAllWorkFlowNames();
        for (String workflowName : workflowNamesList) {
            workflows.add(loadWorkflow(workflowName));
        }
        return workflows;
    }
}

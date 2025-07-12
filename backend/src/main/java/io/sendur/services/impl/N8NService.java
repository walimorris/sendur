package io.sendur.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.sendur.models.leads.ApprovedLeadsWebhookResult;
import io.sendur.models.leads.Lead;
import io.sendur.models.workflows.Node;
import io.sendur.models.workflows.Parameters;
import io.sendur.models.workflows.Workflow;
import io.sendur.models.workflows.WorkflowConverter;
import io.sendur.repositories.LeadRepository;
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
    private final ResourceLoaderService resourceLoaderService;

    @Autowired
    public N8NService(final N8NGatewayService n8nGatewayService,
                      final LeadRepository leadRepository,
                      final ResourceLoaderService resourceLoaderService) {
        this.n8nGatewayService = n8nGatewayService;
        this.leadRepository = leadRepository;
        this.resourceLoaderService = resourceLoaderService;
    }

    public ApprovedLeadsWebhookResult sendEmailsToLeads(List<Lead> leads) {
        return n8nGatewayService.sendApprovedEmailsToLeads(this.leadRepository, leads);
    }

    /**
     * Get a workflow node from a workflow's name and the node id of the searched workflow node.
     *
     * @param workflowName {@link String} workflow name
     * @param nodeId {@link UUID} workflow nodeId
     *
     * @return {@link Node}
     */
    public Node getWorkflowNodeFromNameAndNodeId(String workflowName, UUID nodeId) {
        Workflow searchedWorkflow = loadWorkflow(workflowName);
        if (searchedWorkflow == null) {
            return null;
        }
        List<Node> nodes = searchedWorkflow.getNodes();
        for (Node node : nodes) {
            if (node.getID().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Get a workflow's node given the search node id.
     *
     * @param nodeId {@link UUID} searched node id
     *
     * @return {@link Node} node with given node id
     */
    public Node getWorkFlowNodeFromNodeId(UUID nodeId) {
        List<Workflow> workflows = loadAllWorkflows();
        for (Workflow workflow : workflows) {
            List<Node> currentNodes = workflow.getNodes();
            for (Node node : currentNodes) {
                if (StringUtils.equals(node.getID().toString(), nodeId.toString())) {
                    return node;
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
        return resourceLoaderService.getAiAgentWorkFlowNames();
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
    public Map<UUID, String> getLlmPromptsFromWorkflow(String workflowName) {
        // iterate nodes and get all the llm nodes
        Workflow workflow = loadWorkflow(workflowName);
        if (workflow != null) {
            List<Node> nodes = workflow.getNodes();
            Map<UUID, String> prompts = new HashMap<>();
            for (Node node : nodes) {
                if (node.getType().equals(LLM_NODE)) {
                    // get the parameter that contain the prompt
                    Parameters parameters = node.getParameters();
                    if (StringUtils.isEmpty(parameters.getText())) {
                        prompts.put(node.getID(), "empty prompt - review configuration");
                    } else {
                        prompts.put(node.getID(), parameters.getText());
                    }
                }
            }
            return prompts;
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
            final String workflowJson = resourceLoaderService.loadWorkflowJson(workflowName);
            if (!StringUtils.isEmpty(workflowJson)) {
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
    public boolean removeWorkflowNode(Workflow workflow, UUID nodeId) {
        for (Node node : workflow.getNodes()) {
            if (node.getID().equals(nodeId)) {
                workflow.getNodes().remove(node);
                return true;
            }
        }
        return false;
    }

    /**
     * Load all possible n8n workflows.
     *
     * @return {@link List<Workflow>} list of workflows
     */
    private List<Workflow> loadAllWorkflows() {
        List<Workflow> workflows = new ArrayList<>();
        List<String> workflowNamesList = resourceLoaderService.getAllWorkFlowNames();
        for (String workflowName : workflowNamesList) {
            workflows.add(loadWorkflow(workflowName));
        }
        return workflows;
    }
}

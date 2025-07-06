package io.sendur.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sendur.configurations.N8NConfigurationProperties;
import io.sendur.models.leads.ApprovedLeadsWebhookResult;
import io.sendur.models.leads.Lead;
import io.sendur.models.leads.WebhookMessageId;
import io.sendur.models.workflows.Node;
import io.sendur.models.workflows.Parameters;
import io.sendur.models.workflows.Workflow;
import io.sendur.models.workflows.WorkflowConverter;
import io.sendur.repositories.LeadRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class N8NService {
    private static final Logger LOGGER = LoggerFactory.getLogger(N8NService.class);

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String USER_AGENT = "User-Agent";
    private static final String APPLICATION_JSON = "application/json";
    private static final String APP_NAME = "Sendur";

    // n8n parameter types - make this an ENUM
    private static final String LLM_NODE = "@n8n/n8n-nodes-langchain.agent";

    private final LeadRepository leadRepository;
    private final N8NConfigurationProperties n8NConfigurationProperties;
    private final ResourceLoaderService resourceLoaderService;

    @Autowired
    public N8NService(final LeadRepository leadRepository,
                      final N8NConfigurationProperties n8NConfigurationProperties,
                      final ResourceLoaderService resourceLoaderService) {
        this.leadRepository = leadRepository;
        this.n8NConfigurationProperties = n8NConfigurationProperties;
        this.resourceLoaderService = resourceLoaderService;
    }

    /**
     * Sends approved leads to the n8n approved emails webhook and returns the result.
     *
     * @param leads approved leads
     *
     * @return {@link ApprovedLeadsWebhookResult}
     */
    public ApprovedLeadsWebhookResult sendApprovedEmailsToLeads(List<Lead> leads) {
        try (ClassicHttpResponse response = hitN8NApprovedEmailWebhook(leads)) {
            int statusCode = response.getCode();
            String content = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
            List<WebhookMessageId> webhookMessageIdList = mapper.readValue(content, new TypeReference<>() {});
            if (statusCode == 200) {
                leadRepository.saveAll(leads);
            }
            return new ApprovedLeadsWebhookResult(statusCode, webhookMessageIdList);
        } catch (Exception e) {
            LOGGER.error("Failed to send and save approved leads: {}", e.getMessage(), e);
            return new ApprovedLeadsWebhookResult(500, null);
        }
    }

    /**
     * Sends {@linkplain Lead leads} to n8n approved email webhook.
     *
     * @param leads approved leads
     *
     * @return {@linkplain ClassicHttpResponse HttpResponse}
     *
     * @throws JsonProcessingException leads are malformed
     */
    private ClassicHttpResponse hitN8NApprovedEmailWebhook(List<Lead> leads) throws JsonProcessingException {
        return postN8NWebhook(n8NConfigurationProperties.getApprovedEmailsWebhook(),
                n8NConfigurationProperties.getTimeout(), leads);
    }

    /**
     * Sends a {@linkplain HttpPost POST request} to the given n8n webhook.
     *
     * @param webhook n8n webhook target
     * @param timeout request timeout
     * @param object request body
     *
     * @return {@linkplain ClassicHttpResponse HttpResponse}
     *
     * @throws JsonProcessingException request body is malformed
     */
    private ClassicHttpResponse postN8NWebhook(String webhook, long timeout, Object object) throws JsonProcessingException {
        String json = new ObjectMapper().writeValueAsString(object);
        RequestConfig config = RequestConfig.custom()
                .setResponseTimeout(timeout, TimeUnit.SECONDS)
                .build();
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build()) {
            HttpPost post = new HttpPost(webhook);
            post.setHeader(CONTENT_TYPE, APPLICATION_JSON);
            post.setHeader(USER_AGENT, APP_NAME);
            post.setEntity(new StringEntity(json));

            if (n8nSocketAccepting()) {
                return client.execute(post);
            }
        } catch (IOException | IllegalStateException e) {
            LOGGER.error("Failed to send POST request to N8N webhook {}: {}", webhook, e.getMessage());
        }
        return null;
    }

    /**
     * Determining if n8n server is accepting communication after attempting socket connection
     * from Sendur.
     *
     * @return boolean
     *
     * @throws IllegalStateException this is not wanted state
     */
    private boolean n8nSocketAccepting() throws IllegalStateException {
        final String host = n8NConfigurationProperties.getHost();
        final int port = n8NConfigurationProperties.getPort();
        try (Socket socket = new Socket(host, port)) {
            if (socket.isConnected()) {
                LOGGER.info("n8n HOST '{}' on PORT '{}' is open and accepting", host, port);
                return true;
            } else {
                throw new IllegalStateException("n8n HOST " + host + " on PORT " + port + " is closed and not accepting");
            }
        } catch (IOException e) {
            LOGGER.error("Can't connect to {}:{}: {}", host, port, e.getMessage());
        }
        return false;
    }

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
     * @throws IOException exception serializing workflow
     */
    public Map<UUID, String> getLlmPromptsFromWorkflow(String workflowName) throws IOException {
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
     * @throws IOException exception loading workflow from resource
     */
    private Workflow loadWorkflow(String workflowName) throws IOException {
        final String workflowJson = resourceLoaderService.loadWorkflowJson(workflowName);
        if (!StringUtils.isEmpty(workflowJson)) {
            return WorkflowConverter.fromJsonString(workflowJson);
        }
        return null;
    }
}

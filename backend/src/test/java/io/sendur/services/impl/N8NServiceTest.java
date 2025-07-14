package io.sendur.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sendur.TestUtils;
import io.sendur.models.leads.ApprovedLeadsWebhookResult;
import io.sendur.models.leads.Lead;
import io.sendur.models.leads.WebhookMessageId;
import io.sendur.models.workflows.Node;
import io.sendur.repositories.LeadRepository;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertNull;

@ExtendWith(MockitoExtension.class)
class N8NServiceTest {
    private static LogCaptor logCaptor;

    private static final String PERSISTED_LEADS_RESOURCE = "leads/leads_0.json";
    private static final String MINIMAL_WORKFLOW_RESOURCE = "workflows/workflow_0.json";
    private static final String MINIMAL_WORKFLOW_NAME = "Minimal Test Workflow";
    private static final String MINIMAL_WORKFLOW_NODE_1_NAME = "Test_Node_1";
    private static final UUID MINIMAL_WORKFLOW_NODE_1_UUID;

    static {
        MINIMAL_WORKFLOW_NODE_1_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    }

    @BeforeAll
    public static void setLogCaptor() {
        logCaptor = LogCaptor.forClass(N8NService.class);
    }

    @AfterEach
    public void clearLogs() {
        logCaptor.clearLogs();
    }

    @AfterAll
    public static void tearDown() {
        logCaptor.close();
    }

    @Mock
    private N8NGatewayService n8NGatewayService;

    @Mock
    private ResourceLoaderService resourceLoaderService;

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private N8NService n8NService;

    @Test
    void sendEmailsToLeads() {
        List<Lead> receivedLeads = TestUtils.getObjectFromJsonResourceInputStream(PERSISTED_LEADS_RESOURCE, new TypeReference<>(){});
        WebhookMessageId webhookMessageId1 = new WebhookMessageId("123456789");
        WebhookMessageId webhookMessageId2 = new WebhookMessageId("987654321");
        List<WebhookMessageId> messageIds = List.of(webhookMessageId1, webhookMessageId2);
        ApprovedLeadsWebhookResult approvedLeadsWebhookResult = new ApprovedLeadsWebhookResult(200, messageIds);
        when(n8NGatewayService.sendApprovedEmailsToLeads(leadRepository, receivedLeads)).thenReturn(approvedLeadsWebhookResult);
        ApprovedLeadsWebhookResult approvedLeadsWebhookTestResult = n8NService.sendEmailsToLeads(receivedLeads);
        assertEquals(2, approvedLeadsWebhookTestResult.webhookMessageIds().size());
        assertEquals(200, approvedLeadsWebhookTestResult.statusCode());
        verify(n8NGatewayService, times(1)).sendApprovedEmailsToLeads(leadRepository, receivedLeads);
    }

    @Test
    void getWorkflowNodeFromNameAndNodeId() {
        String minimalWorkflowJson = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE);
        when(resourceLoaderService.loadWorkflowJson(MINIMAL_WORKFLOW_NAME)).thenReturn(minimalWorkflowJson);
        Node minimaloWorkflowNode = n8NService.getWorkflowNodeFromNameAndNodeId(MINIMAL_WORKFLOW_NAME, MINIMAL_WORKFLOW_NODE_1_UUID);
        assertEquals(MINIMAL_WORKFLOW_NODE_1_NAME, minimaloWorkflowNode.getName());
        verify(resourceLoaderService, times(1)).loadWorkflowJson(MINIMAL_WORKFLOW_NAME);
    }

    @Test
    void getWorkFlowNodeFromNameAndNodeIdNullName() {
        Node workflowNode = n8NService.getWorkflowNodeFromNameAndNodeId(null, MINIMAL_WORKFLOW_NODE_1_UUID);
        assertNull("WorkflowNode is null as intended.", workflowNode);
        verify(resourceLoaderService, times(0)).loadWorkflowJson(anyString());
    }

    @Test
    void getWorkflowNodeFromNameAndNodeIdNullNodeId() {
        Node workflowNode = n8NService.getWorkflowNodeFromNameAndNodeId(MINIMAL_WORKFLOW_NAME, null);
        assertNull("workflowNode is null as intended.", workflowNode);
        verify(resourceLoaderService, times(0)).loadWorkflowJson(anyString());
    }

    @Test
    void getWorkFlowNodeFromNodeId() {
    }

    @Test
    void getWorkflowNamesWithAiAgents() {
    }

    @Test
    void getLlmPromptsFromWorkflow() {
    }

    @Test
    void loadWorkflow() {
    }

    @Test
    void saveWorkflow() {
    }

    @Test
    void removeWorkflowNode() {
    }
}
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

    private static final String MINIMAL_WORKFLOW_RESOURCE_0 = "workflows/workflow_0.json";
    private static final String MINIMAL_WORKFLOW_RESOURCE_1 = "workflows/workflow_1.json";

    private static final String MINIMAL_WORKFLOW_NAME_0 = "Minimal Test Workflow 0";
    private static final String MINIMAL_WORKFLOW_NAME_1 = "Minimal Test Workflow 1";

    private static final String MINIMAL_WORKFLOW_NODE_0_NAME = "Test_Node_0";
    private static final String MINIMAL_WORKFLOW_NODE_1_NAME = "Test_Node_1";
    private static final UUID MINIMAL_WORKFLOW_NODE_0_UUID;
    private static final UUID MINIMAL_WORKFLOW_NODE_1_UUID;
    private static final UUID UNKNOWN_WORKFLOW_NODE_UUID;

    static {
        MINIMAL_WORKFLOW_NODE_0_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        MINIMAL_WORKFLOW_NODE_1_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        UNKNOWN_WORKFLOW_NODE_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174123");
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
        String minimalWorkflowJson = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_0);
        when(resourceLoaderService.loadWorkflowJson(MINIMAL_WORKFLOW_NAME_0)).thenReturn(minimalWorkflowJson);
        Node minimaloWorkflowNode = n8NService.getWorkflowNodeFromNameAndNodeId(MINIMAL_WORKFLOW_NAME_0, MINIMAL_WORKFLOW_NODE_0_UUID);
        assertEquals(MINIMAL_WORKFLOW_NODE_0_NAME, minimaloWorkflowNode.getName());
        verify(resourceLoaderService, times(1)).loadWorkflowJson(MINIMAL_WORKFLOW_NAME_0);
    }

    @Test
    void getWorkFlowNodeFromNameAndNodeIdNullName() {
        Node workflowNode = n8NService.getWorkflowNodeFromNameAndNodeId(null, MINIMAL_WORKFLOW_NODE_0_UUID);
        assertNull("WorkflowNode is null as intended.", workflowNode);
        verify(resourceLoaderService, times(0)).loadWorkflowJson(anyString());
    }

    @Test
    void getWorkflowNodeFromNameAndNodeIdNullNodeId() {
        Node workflowNode = n8NService.getWorkflowNodeFromNameAndNodeId(MINIMAL_WORKFLOW_NAME_0, null);
        assertNull("WorkflowNode is null as intended.", workflowNode);
        verify(resourceLoaderService, times(0)).loadWorkflowJson(anyString());
    }

    @Test
    void getWorkFlowNodeFromNodeId() {
        List<String> workflowNames = List.of(MINIMAL_WORKFLOW_NAME_0, MINIMAL_WORKFLOW_NAME_1);
        String minimalWorkflowJson0 = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_0);
        String minimalWorkflowJson1 = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_1);

        when(resourceLoaderService.getAllWorkFlowNames()).thenReturn(workflowNames);
        when(resourceLoaderService.loadWorkflowJson(MINIMAL_WORKFLOW_NAME_0)).thenReturn(minimalWorkflowJson0);
        when(resourceLoaderService.loadWorkflowJson(MINIMAL_WORKFLOW_NAME_1)).thenReturn(minimalWorkflowJson1);

        Node searchedNode = n8NService.getWorkFlowNodeFromNodeId(MINIMAL_WORKFLOW_NODE_0_UUID);
        assertEquals(MINIMAL_WORKFLOW_NODE_0_NAME, searchedNode.getName());
        assertEquals(MINIMAL_WORKFLOW_NODE_0_UUID, searchedNode.getID());
        verify(resourceLoaderService, times(1)).getAllWorkFlowNames();
        verify(resourceLoaderService, times(2)).loadWorkflowJson(anyString());
    }

    @Test
    void getWorkflowNodeFromNodeIdNullUUID() {
        Node searchedNode = n8NService.getWorkFlowNodeFromNodeId(null);
        assertNull("WorkflowNode is null as intended.", searchedNode );
        verify(resourceLoaderService, times(0)).getAllWorkFlowNames();
        verify(resourceLoaderService, times(0)).loadWorkflowJson(anyString());
    }

    @Test
    void getWorkflowNodeFromNodeIdUnknownUUID() {
        List<String> workflowNames = List.of(MINIMAL_WORKFLOW_NAME_0, MINIMAL_WORKFLOW_NAME_1);
        String minimalWorkflowJson0 = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_0);
        String minimalWorkflowJson1 = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_1);

        when(resourceLoaderService.getAllWorkFlowNames()).thenReturn(workflowNames);
        when(resourceLoaderService.loadWorkflowJson(MINIMAL_WORKFLOW_NAME_0)).thenReturn(minimalWorkflowJson0);
        when(resourceLoaderService.loadWorkflowJson(MINIMAL_WORKFLOW_NAME_1)).thenReturn(minimalWorkflowJson1);

        Node searchedNode = n8NService.getWorkFlowNodeFromNodeId(UNKNOWN_WORKFLOW_NODE_UUID);
        assertNull("WorkflowNode is null as intended with valid nodes, but unknown UUID", searchedNode);
        verify(resourceLoaderService, times(1)).getAllWorkFlowNames();
        verify(resourceLoaderService, times(2)).loadWorkflowJson(anyString());
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
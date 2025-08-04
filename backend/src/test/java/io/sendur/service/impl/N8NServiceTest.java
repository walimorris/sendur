package io.sendur.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sendur.TestUtils;
import io.sendur.domain.lead.ApprovedLeadsWebhookResult;
import io.sendur.domain.lead.Lead;
import io.sendur.domain.lead.WebhookMessageId;
import io.sendur.domain.workflow.Workflow;
import io.sendur.domain.workflow.WorkflowConverter;
import io.sendur.repository.LeadRepository;
import nl.altindag.log.LogCaptor;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.*;

@ExtendWith(MockitoExtension.class)
class N8NServiceTest {
    private static LogCaptor logCaptor;

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
    private WorkflowService workflowService;

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private N8NService n8NService;

    private static final String PERSISTED_LEADS_RESOURCE = "leads/leads_0.json";

    private static final String MINIMAL_WORKFLOW_RESOURCE_0 = "workflows/workflow_0.json";
    private static final String MINIMAL_WORKFLOW_RESOURCE_1 = "workflows/workflow_1.json";
    private static final String AI_WORKFLOW_RESOURCE_0 = "workflows/ai_workflow_0.json";

    private static final String MINIMAL_WORKFLOW_NAME_0 = "Minimal Test Workflow 0";
    private static final String MINIMAL_WORKFLOW_NAME_1 = "Minimal Test Workflow 1";
    private static final String AI_WORKFLOW_NODE_NAME = "Test AI Node 0";
    private static final String UNKNOWN_WORKFLOW_NAME = "Unknown Workflow";

    private static final String MINIMAL_WORKFLOW_NODE_0_NAME = "Test_Node_0";
    private static final String MINIMAL_WORKFLOW_NODE_1_NAME = "Test_Node_1";
    private static final String AI_WORKFLOW_NODE_0_NAME = "AI Agent 0";

    private static final String MINIMAL_WORKFLOW_NODE_0_UUID;
    private static final String MINIMAL_WORKFLOW_NODE_1_UUID;
    private static final String AI_WORKFLOW_NODE_0_UUID;
    private static final String UNKNOWN_WORKFLOW_NODE_UUID;

    private static final String AI_WORKFLOW_NODE_0_PROMPT = "Your task is to find 50 small mom-and-pop service businesses...";

    private static final String AI_WORKFLOW_1 = "AI_Agent___Scheduled_Lead_Contact_Updater.json";
    private static final String AI_WORKFLOW_2 = "AI_Agent___Scheduled_Lead_Generator.json";
    private static final String AI_WORKFLOW_3 = "AI_Agent___Send_Approved_Emails_Webhook.json";
    private static final String AI_WORKFLOW_4 = "aI_aGENT___Schedule_Lead_Follow_Up";

    private static final String TASK_WORKFLOW_1 = "TASK___Load_Sent_Emails";
    private static final String TASK_WORKFLOW_2 = "TASK___Load_Failed_Emails";

    static {
        MINIMAL_WORKFLOW_NODE_0_UUID = "123e4567-e89b-12d3-a456-426614174000";
        MINIMAL_WORKFLOW_NODE_1_UUID = "123e4567-e89b-12d3-a456-426614174001";
        AI_WORKFLOW_NODE_0_UUID = "12345678-1234-1234-1234-123456789abc";
        UNKNOWN_WORKFLOW_NODE_UUID = "123e4567-e89b-12d3-a456-426614174123";
    }

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
        when(workflowService.loadWorkflowJson(MINIMAL_WORKFLOW_NAME_0)).thenReturn(minimalWorkflowJson);
        Map<String, Object> minimalWorkflowNode = n8NService.getWorkflowNodeFromNameAndNodeId(MINIMAL_WORKFLOW_NAME_0, MINIMAL_WORKFLOW_NODE_0_UUID);
        String nodeName = (String) minimalWorkflowNode.getOrDefault("name", "");
        assertEquals(MINIMAL_WORKFLOW_NODE_0_NAME, nodeName);
        verify(workflowService, times(1)).loadWorkflowJson(MINIMAL_WORKFLOW_NAME_0);
    }

    @Test
    void getWorkFlowNodeFromNameAndNodeIdNullName() {
        Map<String, Object> workflowNode = n8NService.getWorkflowNodeFromNameAndNodeId(null, MINIMAL_WORKFLOW_NODE_0_UUID);
        assertNull("WorkflowNode is null as intended.", workflowNode);
        verify(workflowService, times(0)).loadWorkflowJson(anyString());
    }

    @Test
    void getWorkflowNodeFromNameAndNodeIdNullNodeId() {
        Map<String, Object> workflowNode = n8NService.getWorkflowNodeFromNameAndNodeId(MINIMAL_WORKFLOW_NAME_0, null);
        assertNull("WorkflowNode is null as intended.", workflowNode);
        verify(workflowService, times(0)).loadWorkflowJson(anyString());
    }

    @Test
    void getWorkFlowNodeFromNodeId() {
        List<String> workflowNames = List.of(MINIMAL_WORKFLOW_NAME_0, MINIMAL_WORKFLOW_NAME_1);
        String minimalWorkflowJson0 = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_0);
        String minimalWorkflowJson1 = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_1);

        when(workflowService.getAllWorkFlowNames()).thenReturn(workflowNames);
        when(workflowService.loadWorkflowJson(MINIMAL_WORKFLOW_NAME_0)).thenReturn(minimalWorkflowJson0);
        when(workflowService.loadWorkflowJson(MINIMAL_WORKFLOW_NAME_1)).thenReturn(minimalWorkflowJson1);

        Map<String, Object> searchedNode = n8NService.getWorkFlowNodeFromNodeId(MINIMAL_WORKFLOW_NODE_0_UUID);
        String id = (String) searchedNode.getOrDefault("id", "");
        String name = (String) searchedNode.getOrDefault("name", "");
        assertEquals(MINIMAL_WORKFLOW_NODE_0_UUID, id);
        assertEquals(MINIMAL_WORKFLOW_NODE_0_NAME, name);
        verify(workflowService, times(1)).getAllWorkFlowNames();
        verify(workflowService, times(2)).loadWorkflowJson(anyString());
    }

    @Test
    void getWorkflowNodeFromNodeIdNullUUID() {
        Map<String, Object> searchedNode = n8NService.getWorkFlowNodeFromNodeId(null);
        assertNull("WorkflowNode is null as intended.", searchedNode );
        verify(workflowService, times(0)).getAllWorkFlowNames();
        verify(workflowService, times(0)).loadWorkflowJson(anyString());
    }

    @Test
    void getWorkflowNodeFromNodeIdUnknownUUID() {
        List<String> workflowNames = List.of(MINIMAL_WORKFLOW_NAME_0, MINIMAL_WORKFLOW_NAME_1);
        String minimalWorkflowJson0 = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_0);
        String minimalWorkflowJson1 = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_1);

        when(workflowService.getAllWorkFlowNames()).thenReturn(workflowNames);
        when(workflowService.loadWorkflowJson(MINIMAL_WORKFLOW_NAME_0)).thenReturn(minimalWorkflowJson0);
        when(workflowService.loadWorkflowJson(MINIMAL_WORKFLOW_NAME_1)).thenReturn(minimalWorkflowJson1);

        Map<String, Object> searchedNode = n8NService.getWorkFlowNodeFromNodeId(UNKNOWN_WORKFLOW_NODE_UUID);
        assertNull("WorkflowNode is null as intended with valid nodes, but unknown UUID", searchedNode);
        verify(workflowService, times(1)).getAllWorkFlowNames();
        verify(workflowService, times(2)).loadWorkflowJson(anyString());
    }

    @Test
    void getLlmPromptsFromWorkflow() {
        String aiWorkflowJson = TestUtils.getStringContentFromResource(AI_WORKFLOW_RESOURCE_0);
        when(workflowService.loadWorkflowJson(AI_WORKFLOW_NODE_NAME)).thenReturn(aiWorkflowJson);
        Map<String, String> prompts = n8NService.getLlmPromptsFromWorkflow(AI_WORKFLOW_NODE_NAME);

        Set<String> resultsPrompts = new HashSet<>();
        Set<String> uuids = prompts.keySet();
        for (String uuid : uuids) {
            resultsPrompts.add(prompts.get(uuid));
        }
        assertNotNull("Map contains LLM Node's UUID and prompt.", prompts);
        assertTrue("Map contains correct UUID for LLM Node.", uuids.contains(AI_WORKFLOW_NODE_0_UUID));
        assertTrue("Map contains correct prompt for LLM Node.", resultsPrompts.contains(AI_WORKFLOW_NODE_0_PROMPT));
        verify(workflowService, times(1)).loadWorkflowJson(anyString());
    }

    @Test
    void getLlmPromptFromWorkflowNoLlmNodes() {
        String workflowJson = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_1);
        when(workflowService.loadWorkflowJson(MINIMAL_WORKFLOW_NAME_1)).thenReturn(workflowJson);
        Map<String, String> prompts1 = n8NService.getLlmPromptsFromWorkflow(MINIMAL_WORKFLOW_NAME_1);
        Map<String, String> prompts2 = n8NService.getLlmPromptsFromWorkflow(StringUtils.EMPTY);
        assertEquals(0, prompts1.size());
        assertEquals(0, prompts2.size());
        verify(workflowService, times(1)).loadWorkflowJson(anyString());
    }

    @Test
    void removeWorkflowNode() throws IOException {
        String workflowJson = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_1);
        Workflow workflow = WorkflowConverter.fromJsonString(workflowJson);
        boolean wasRemoved1 = n8NService.removeWorkflowNode(workflow, MINIMAL_WORKFLOW_NODE_1_UUID);
        boolean wasRemoved2 = n8NService.removeWorkflowNode(workflow, UNKNOWN_WORKFLOW_NODE_UUID);
        boolean wasRemoved3 = n8NService.removeWorkflowNode(workflow, null);

        // it would probably make sense to save the workflow after node deletion and validate node is indeed removed
        assertTrue("WorkflowNode was removed as intended.", wasRemoved1);
        assertFalse("WorkflowNode was not removed as intended, node not found in workflow.", wasRemoved2);
        assertFalse("WorkflowNode was not removed as intended, UUID is null.", wasRemoved3);
    }

    @Test
    void loadWorkflow() throws IOException {
        String workflowJson1 = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_1);
        Workflow expectedWorkflow1 = WorkflowConverter.fromJsonString(workflowJson1);
        String expectedWorkflowId1 = expectedWorkflow1.getWorkflowId();
        when(workflowService.loadWorkflowJson(MINIMAL_WORKFLOW_NAME_1)).thenReturn(workflowJson1);

        Workflow workflowResult1 = n8NService.loadWorkflow(MINIMAL_WORKFLOW_NAME_1);
        String workflowResultId1 = workflowResult1.getWorkflowId();
        assertEquals(expectedWorkflow1.getName(), workflowResult1.getName());
        assertEquals(expectedWorkflowId1, workflowResultId1);
        assertEquals(expectedWorkflow1.getNodes().size(), workflowResult1.getNodes().size());
        verify(workflowService, times(1)).loadWorkflowJson(anyString());
    }

    @Test
    void loadWorkflowUnknown() {
        when(workflowService.loadWorkflowJson(UNKNOWN_WORKFLOW_NAME)).thenReturn(null);

        Workflow workflowResult = n8NService.loadWorkflow(UNKNOWN_WORKFLOW_NAME);
        assertNull("Workflow null as intended, resource unknown/not found.", workflowResult);
        verify(workflowService, times(1)).loadWorkflowJson(anyString());
    }

    @Test
    void loadWorkflowEmpty() {
        Workflow workflow = n8NService.loadWorkflow(StringUtils.EMPTY);

        assertNull("Workflow null as intended with empty workflow name.", workflow);
        verify(workflowService, times(0)).loadWorkflowJson(anyString());
    }

//    @Test
//    void getWorkflowNamesWithAiAgents() {
//        List<String> workflowNames = List.of(
//                AI_WORKFLOW_1,
//                AI_WORKFLOW_2,
//                AI_WORKFLOW_3,
//                AI_WORKFLOW_4,
//                TASK_WORKFLOW_1,
//                TASK_WORKFLOW_2
//        );
//        when(resourceLoaderService.getAllWorkFlowNames()).thenReturn(workflowNames);
//        List<String> result = n8NService.getWorkflowNamesWithAiAgents();
//        assertEquals(4, result.size());
//        result.forEach(name -> {
//            assertTrue("Name contains correct prefix.", StringUtils.containsIgnoreCase(name, "AI_Agent"));
//        });
//    }

    @Test
    void saveWorkflow() {
    }
}
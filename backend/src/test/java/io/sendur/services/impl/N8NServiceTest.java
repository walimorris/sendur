package io.sendur.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sendur.TestUtils;
import io.sendur.models.leads.ApprovedLeadsWebhookResult;
import io.sendur.models.leads.Lead;
import io.sendur.models.leads.WebhookMessageId;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class N8NServiceTest {
    private static LogCaptor logCaptor;

    private static final String PERSISTED_LEADS_RESOURCE = "Leads/leads_0.json";

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
    }

    @Test
    void getWorkflowNodeFromNameAndNodeId() {

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
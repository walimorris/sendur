package io.sendur.service.impl;

import io.sendur.TestUtils;
import io.sendur.component.cache.WorkflowSessionCache;
import io.sendur.domain.workflow.Workflow;
import io.sendur.domain.workflow.WorkflowConverter;
import io.sendur.repository.WorkflowRepository;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {
    private static LogCaptor logCaptor;

    @BeforeAll
    public static void setLogCaptor() {
        logCaptor = LogCaptor.forClass(WorkflowService.class);
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
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowSessionCache workflowSessionCache;

    @InjectMocks
    private WorkflowService workflowService;

    private static final String MINIMAL_WORKFLOW_RESOURCE_1 = "workflows/workflow_1.json";
    private static final String MINIMAL_WORKFLOW_NAME_1 = "Minimal Test Workflow 1";

    private static final String MINIMAL_WORKFLOW_RESOURCE_2 = "workflows/workflow_2.json";
    private static final String MINIMAL_WORKFLOW_NAME_2 = "Minimal Test Workflow 2";
    private static final String MINIMAL_WORKFLOW_ID_2 = "PMQuvLzHctXRBhXx";


    @Test
    void saveWorkflow_newWorkflow() throws IOException {
        String workflowJson = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_2);
        Workflow workflow = WorkflowConverter.fromJsonString(workflowJson);

        when(workflowRepository.findByWorkflowId(MINIMAL_WORKFLOW_ID_2)).thenReturn(Optional.empty());
        when(workflowRepository.save(workflow)).thenReturn(workflow);
        when(workflowSessionCache.add(workflow)).thenReturn(true);

        Workflow workflowResult = workflowService.saveWorkflow(workflow);

        assertEquals(MINIMAL_WORKFLOW_ID_2, workflowResult.getWorkflowId());
        verify(workflowRepository, times(1)).findByWorkflowId(MINIMAL_WORKFLOW_ID_2);
        verify(workflowRepository, times(1)).save(workflow);
        verify(workflowSessionCache, times(1)).add(workflow);
    }

    @Test
    void saveWorkflow_existingWorkflow() throws IOException {
        String workflowJson = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_2);
        Workflow workflow = WorkflowConverter.fromJsonString(workflowJson);

        // this basically says: this workflow that's being saved already exist
        when(workflowRepository.findByWorkflowId(MINIMAL_WORKFLOW_ID_2)).thenReturn(Optional.of(workflow));

        Workflow workflowResult = workflowService.saveWorkflow(workflow);

        assertEquals(MINIMAL_WORKFLOW_ID_2, workflowResult.getWorkflowId());
        assertTrue(logCaptor.getLogs().get(0).contains("Workflow with workflowId already exists " + workflowResult.getWorkflowId()));
        verify(workflowRepository, times(1)).findByWorkflowId(MINIMAL_WORKFLOW_ID_2);
        verify(workflowRepository, times(0)).save(any(Workflow.class));
        verify(workflowSessionCache, times(0)).add(any(Workflow.class));
    }

    @Test
    void findAllWorkflows_notCached() throws IOException {
        String workflow1Json = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_1);
        String workflow2Json = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_2);
        Workflow workflow1 = WorkflowConverter.fromJsonString(workflow1Json);
        Workflow workflow2 = WorkflowConverter.fromJsonString(workflow2Json);

        List<Workflow> workflowList = List.of(workflow1, workflow2);

        when(workflowSessionCache.isCached()).thenReturn(false);
        when(workflowRepository.findAll()).thenReturn(workflowList);
        doNothing().when(workflowSessionCache).set(workflowList);

        List<Workflow> allWorkflowsResult = workflowService.findAllWorkflows();

        assertEquals(2, allWorkflowsResult.size(), "WorkflowList should be size 2");
        verify(workflowRepository, times(1)).findAll();
        verify(workflowSessionCache, times(1)).isCached();
        verify(workflowSessionCache, times(1)).set(workflowList);
    }

    @Test
    void findAllWorkflows_cached() throws IOException {
        String workflow1Json = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_1);
        String workflow2Json = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_2);
        Workflow workflow1 = WorkflowConverter.fromJsonString(workflow1Json);
        Workflow workflow2 = WorkflowConverter.fromJsonString(workflow2Json);

        List<Workflow> workflowList = List.of(workflow1, workflow2);

        when(workflowSessionCache.isCached()).thenReturn(true);
        when(workflowSessionCache.get()).thenReturn(workflowList);

        List<Workflow> allWorkflowsResult = workflowService.findAllWorkflows();

        assertEquals(2, allWorkflowsResult.size(), "WorkflowList should return from WorkflowSessionCache at size 2");
        verify(workflowRepository, times(0)).findAll();
        verify(workflowSessionCache, times(1)).isCached();
        verify(workflowSessionCache, times(1)).get();
    }

    @Test
    void refresh() throws IOException {
        String workflow1Json = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_1);
        String workflow2Json = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_2);
        Workflow workflow1 = WorkflowConverter.fromJsonString(workflow1Json);
        Workflow workflow2 = WorkflowConverter.fromJsonString(workflow2Json);

        List<Workflow> workflowList = List.of(workflow1, workflow2);

        when(workflowRepository.findAll()).thenReturn(workflowList);
        doNothing().when(workflowSessionCache).set(workflowList);

        workflowService.refresh();

        verify(workflowRepository, times(1)).findAll();
        verifyNoMoreInteractions(workflowRepository);
        verify(workflowSessionCache, times(1)).set(workflowList);
        verifyNoMoreInteractions(workflowSessionCache);
    }

    @Test
    void getAllWorkFlowNames_fromCache() throws IOException {
        String workflow1Json = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_1);
        String workflow2Json = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_2);
        Workflow workflow1 = WorkflowConverter.fromJsonString(workflow1Json);
        Workflow workflow2 = WorkflowConverter.fromJsonString(workflow2Json);

        List<Workflow> workflowList = List.of(workflow1, workflow2);

        when(workflowSessionCache.isCached()).thenReturn(true);
        when(workflowSessionCache.get()).thenReturn(workflowList);

        List<String> allWorkflowNames = workflowService.getAllWorkFlowNames();

        assertEquals(2, allWorkflowNames.size());
        assertTrue(allWorkflowNames.contains(MINIMAL_WORKFLOW_NAME_1));
        assertTrue(allWorkflowNames.contains(MINIMAL_WORKFLOW_NAME_2));
        verify(workflowSessionCache, times(1)).isCached();
        verify(workflowSessionCache, times(1)).get();
        verifyNoMoreInteractions(workflowSessionCache);
        verifyNoInteractions(workflowRepository);
    }

    @Test
    void getAllWorkflowNames_fromRepository() throws IOException {
        String workflow1Json = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_1);
        String workflow2Json = TestUtils.getStringContentFromResource(MINIMAL_WORKFLOW_RESOURCE_2);
        Workflow workflow1 = WorkflowConverter.fromJsonString(workflow1Json);
        Workflow workflow2 = WorkflowConverter.fromJsonString(workflow2Json);

        List<Workflow> workflowList = List.of(workflow1, workflow2);

        when(workflowSessionCache.isCached()).thenReturn(false);
        when(workflowRepository.findAll()).thenReturn(workflowList);
        doNothing().when(workflowSessionCache).set(workflowList);

        List<String> allWorkflowNames = workflowService.getAllWorkFlowNames();

        assertEquals(2, allWorkflowNames.size());
        assertTrue(allWorkflowNames.contains(MINIMAL_WORKFLOW_NAME_1));
        assertTrue(allWorkflowNames.contains(MINIMAL_WORKFLOW_NAME_2));
        verify(workflowSessionCache, times(1)).isCached();
        verify(workflowSessionCache, times(1)).set(workflowList);
        verifyNoMoreInteractions(workflowSessionCache);
        verify(workflowRepository, times(1)).findAll();
        verifyNoMoreInteractions(workflowRepository);
    }

    @Test
    void getAllWorkflowNames_emptyWorkflows() {
        when(workflowSessionCache.isCached()).thenReturn(false);
        when(workflowRepository.findAll()).thenReturn(new ArrayList<>());

        List<String> allWorkflowNames = workflowService.getAllWorkFlowNames();

        assertTrue(allWorkflowNames.isEmpty());
        verify(workflowSessionCache, times(1)).isCached();
        verifyNoMoreInteractions(workflowSessionCache);
        verify(workflowRepository, times(1)).findAll();
        verifyNoMoreInteractions(workflowRepository);
        assertTrue(logCaptor.getLogs().get(0).contains("Warning: workflows not available in cache or database." +
                " Validate this is intended."));
    }

    @Test
    void getAiAgentWorkFlowNames() {
    }

    @Test
    void loadWorkflowJson() {
    }

    @Test
    void toJsonString() {
    }
}
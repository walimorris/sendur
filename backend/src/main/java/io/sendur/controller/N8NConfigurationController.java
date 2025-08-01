package io.sendur.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.sendur.domain.workflow.Node;
import io.sendur.domain.workflow.WorkFlowPrompt;
import io.sendur.domain.workflow.Workflow;
import io.sendur.security.AuthService;
import io.sendur.service.impl.AiGuardRailsService;
import io.sendur.service.impl.N8NService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/sendur/api/n8n/configuration")
public class N8NConfigurationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(N8NConfigurationController.class);

    private final N8NService n8nService;
    private final AuthService authService;
    private final AiGuardRailsService aiGuardRailsService;

    @Autowired
    public N8NConfigurationController(final AuthService authservice, final N8NService n8nService, AiGuardRailsService aiGuardRailsService) {
        this.n8nService = n8nService;
        this.authService = authservice;
        this.aiGuardRailsService = aiGuardRailsService;
    }

    @PreAuthorize("hasAuthority('OIDC_USER')")
    @GetMapping("/receive-prompts")
    public ResponseEntity<List<WorkFlowPrompt>> receiveAllLeads(Authentication authentication, @RequestParam String workflowName) {
        if (!authService.isExplicitlyAuthorized(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .lastModified(Instant.now().toEpochMilli())
                    .build();
        }
        Map<UUID, String> prompts = n8nService.getLlmPromptsFromWorkflow(workflowName);
        List<WorkFlowPrompt> workFlowPrompts = new ArrayList<>();
        for (Map.Entry<UUID, String> entry : prompts.entrySet()) {
            WorkFlowPrompt workFlowPrompt = new WorkFlowPrompt();
            workFlowPrompt.setName(workflowName);
            workFlowPrompt.setId(entry.getKey());
            workFlowPrompt.setPrompt(entry.getValue());
            workFlowPrompts.add(workFlowPrompt);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .lastModified(Instant.now().toEpochMilli())
                .body(workFlowPrompts);
    }

    @PreAuthorize("hasAuthority('OIDC_USER')")
    @GetMapping("/ai-agent-workflow-names")
    public ResponseEntity<List<String>> aiGentWorkflowNames(Authentication authentication) {
        if (!authService.isExplicitlyAuthorized(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .lastModified(Instant.now().toEpochMilli())
                    .build();
        }
        List<String> workflowNames = n8nService.getWorkflowNamesWithAiAgents();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .lastModified(Instant.now().toEpochMilli())
                .body(workflowNames);
    }

    @PreAuthorize("hasAuthority('OIDC_USER')")
    @PostMapping("/save-updated-prompt")
    public ResponseEntity<?> postUpdatedPrompt(Authentication authentication, @RequestBody WorkFlowPrompt updatedWorkFlowPrompt) throws JsonProcessingException {
        if (!authService.isExplicitlyAuthorized(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .lastModified(Instant.now().toEpochMilli())
                    .build();
        }
        Workflow workflow = n8nService.loadWorkflow(updatedWorkFlowPrompt.getName());
        Node workflowPromptNode = n8nService.getWorkflowNodeFromNameAndNodeId(updatedWorkFlowPrompt.getName(), updatedWorkFlowPrompt.getId());
        if (workflowPromptNode == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .lastModified(Instant.now().toEpochMilli())
                    .body("Workflow '" + updatedWorkFlowPrompt.getName() + "' not found");
        }
        // sanitize the prompt and update the workflow, here is where we need to update the json
        // of the actual file and workflow
        Map<String, String> violations = aiGuardRailsService.validatePrompt(updatedWorkFlowPrompt.getPrompt());
        if (!violations.isEmpty()) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .lastModified(Instant.now().toEpochMilli())
                    .body(violations);
        }
        workflowPromptNode.getParameters().setText(updatedWorkFlowPrompt.getPrompt());

        // may need to pass the updateWorkFlowPrompt that has file name format
        boolean hasSavedWorkflow = n8nService.saveWorkflow(workflow);

        if (!hasSavedWorkflow) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .lastModified(Instant.now().toEpochMilli())
                    .body("Workflow '" + updatedWorkFlowPrompt.getName() + "' not successfully saved");
        }
        return ResponseEntity.ok()
                .lastModified(Instant.now().toEpochMilli())
                .build();
    }
}

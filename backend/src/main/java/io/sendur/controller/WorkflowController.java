package io.sendur.controller;

import io.sendur.domain.workflow.Workflow;
import io.sendur.security.AuthService;
import io.sendur.service.impl.WorkflowService;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/sender/api/n8n/workflow")
public class WorkflowController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowController.class);

    private final WorkflowService workflowService;
    private final AuthService authService;

    public WorkflowController(WorkflowService workflowService, AuthService authService) {
        this.workflowService = workflowService;
        this.authService = authService;
    }

    @PreAuthorize("hasAuthority('OIDC_USER')")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadWorkflow(@RequestBody Workflow workflow, Authentication authentication) {
        if (authService.isNotAuthorized(authentication)) {
            return authService.forbiddenResponse();
        }
        if (ObjectUtils.isNotEmpty(workflow)) {
            Workflow savedWorkFlow = workflowService.saveWorkflow(workflow);
            if (ObjectUtils.isNotEmpty(savedWorkFlow)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .lastModified(Instant.now().toEpochMilli())
                        .build();
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .lastModified(Instant.now().toEpochMilli())
                .body("Error saving workflow. Review file content.");
    }

    @PreAuthorize("hasAuthority('OIDC_USER')")
    @GetMapping("/find-all")
    public ResponseEntity<?> receiveAllWorkflows(Authentication authentication) {
        if (authService.isNotAuthorized(authentication)) {
            return authService.forbiddenResponse();
        }
        List<Workflow> workflows = workflowService.findAllWorkflows();
        return ResponseEntity.status(HttpStatus.OK)
                .lastModified(Instant.now().toEpochMilli())
                .contentType(MediaType.APPLICATION_JSON)
                .body(workflows);
    }

    @PreAuthorize("hasAuthority('OIDC_USER')")
    @GetMapping("/flush")
    public ResponseEntity<?> flush(Authentication authentication) {
        if (authService.isNotAuthorized(authentication)) {
            return authService.forbiddenResponse();
        }
        workflowService.refresh();
        return ResponseEntity.status(HttpStatus.OK)
                .lastModified(Instant.now().toEpochMilli())
                .build();
    }
}

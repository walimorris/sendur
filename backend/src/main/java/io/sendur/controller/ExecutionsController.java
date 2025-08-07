package io.sendur.controller;

import io.sendur.domain.execution.ExecutionsResult;
import io.sendur.security.AuthService;
import io.sendur.service.impl.ExecutionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/sender/api/n8n/executions")
public class ExecutionsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionsController.class);

    private final ExecutionsService executionsService;
    private final AuthService authService;

    @Autowired
    public ExecutionsController(ExecutionsService executionsService, AuthService authService) {
        this.executionsService = executionsService;
        this.authService = authService;
    }

    @PreAuthorize("hasAuthority('OIDC_USER')")
    @GetMapping("/get-by-workflow/{workflowId}")
    public ResponseEntity<?> getN8NExecutionsByWorkflowId(@PathVariable String workflowId, Authentication authentication) {
        if (authService.isNotAuthorized(authentication)) {
            return authService.forbiddenResponse();
        }
        ExecutionsResult executionsResult = executionsService.getExecutionsByWorkflowId(workflowId);
        if (executionsResult.statusCode() != 200) {
            return ResponseEntity.status(executionsResult.statusCode())
                    .lastModified(Instant.now().toEpochMilli())
                    .body(executionsResult.message().orElse(""));
        }
        return ResponseEntity.status(executionsResult.statusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .lastModified(Instant.now().toEpochMilli())
                .body(executionsResult.executions());
    }
}

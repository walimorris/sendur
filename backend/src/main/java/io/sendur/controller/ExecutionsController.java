package io.sendur.controller;

import io.sendur.domain.execution.ExecutionsResult;
import io.sendur.security.AuthService;
import io.sendur.service.impl.ExecutionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Set;

@RestController
@RequestMapping("/sender/api/n8n/executions")
public class ExecutionsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionsController.class);

    private final ExecutionsService executionsService;
    private final AuthService authService;

    public ExecutionsController(ExecutionsService executionsService, AuthService authService) {
        this.executionsService = executionsService;
        this.authService = authService;
    }

    @PreAuthorize("hasAuthority('OIDC_USER')")
    @GetMapping("/find-all")
    public ResponseEntity<?> getAllExecutions(Authentication authentication) {
        if (authService.isNotAuthorized(authentication)) {
            return authService.forbiddenResponse();
        }
        return executionsResponse(executionsService.getAllExecutions());
    }

    @PreAuthorize("hasAuthority('OIDC_USER')")
    @GetMapping("/by-execution")
    public ResponseEntity<?> getExecutionsByExecutionId(@RequestParam String executionId, Authentication authentication) {
        if (authService.isNotAuthorized(authentication)) {
            return authService.forbiddenResponse();
        }
        return executionsResponse(executionsService.getExecutionByExecutionId(executionId));
    }

    @PreAuthorize("hasAuthority('OIDC_USER')")
    @GetMapping("/by-executionIds")
    public ResponseEntity<?> getExecutionsByExecutionIdsMini(@RequestParam Set<String> ids, Authentication authentication) {
        if (authService.isNotAuthorized(authentication)) {
            return authService.forbiddenResponse();
        }
        return executionsResponse(executionsService.getExecutionsByExecutionIds(ids));
    }

    @PreAuthorize("hasAuthority('OIDC_USER')")
    @PostMapping("/filter")
    public ResponseEntity<?> getExecutionsByExecutionIdsBulk(@RequestParam Set<String> ids, Authentication authentication) {
        if (authService.isNotAuthorized(authentication)) {
            return authService.forbiddenResponse();
        }
        return executionsResponse(executionsService.getExecutionsByExecutionIds(ids));
    }

    @PreAuthorize("hasAuthority('OIDC_USER')")
    @GetMapping("/by-workflow")
    public ResponseEntity<?> getExecutionsByWorkflowId(@RequestParam String workflowId, Authentication authentication) {
        if (authService.isNotAuthorized(authentication)) {
            return authService.forbiddenResponse();
        }
        return executionsResponse(executionsService.getExecutionsByWorkflowId(workflowId));
    }

    protected ResponseEntity<?> executionsResponse(ExecutionsResult executionsResult) {
        if (executionsResult.statusCode() != 200) {
            return unsuccessfulExecutionsResponse(executionsResult);
        }
        return ResponseEntity.status(executionsResult.statusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .lastModified(Instant.now().toEpochMilli())
                .body(executionsResult.executions());
    }

    protected ResponseEntity<?> unsuccessfulExecutionsResponse(ExecutionsResult executionsResult) {
        return ResponseEntity.status(executionsResult.statusCode())
                .lastModified(Instant.now().toEpochMilli())
                .body(executionsResult.message().orElse(""));
    }
}

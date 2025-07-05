package io.sendur.controllers;

import io.sendur.security.AuthService;
import io.sendur.services.N8NService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/sendur/api/n8n/configuration")
public class N8NConfigurationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(N8NConfigurationController.class);

    private final N8NService n8nService;
    private final AuthService authService;

    @Autowired
    public N8NConfigurationController(final AuthService authservice, final N8NService n8nService) {
        this.n8nService = n8nService;
        this.authService = authservice;
    }

    @PreAuthorize("hasAuthority('OIDC_USER')")
    @GetMapping("/receive-prompts")
    public ResponseEntity<Map<UUID, String>> receiveAllLeads(Authentication authentication, @RequestParam String workflowName) {
        if (!authService.isExplicitlyAuthorized(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .lastModified(Instant.now().toEpochMilli())
                    .build();
        }
        try {
            Map<UUID, String> prompts = n8nService.getLlmPromptsFromWorkflow(workflowName);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .lastModified(Instant.now().toEpochMilli())
                    .body(prompts);
        } catch (IOException e) {
            LOGGER.error("Error getting prompts from workflow {}. {}", workflowName, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

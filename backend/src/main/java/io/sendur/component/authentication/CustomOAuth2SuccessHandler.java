package io.sendur.component.authentication;

import io.sendur.domain.workflow.Workflow;
import io.sendur.service.impl.WorkflowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomOAuth2SuccessHandler.class);

    private final WorkflowService workflowService;

    public CustomOAuth2SuccessHandler(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        List<Workflow> userWorkflows = workflowService.findAllWorkflows();
        if (ObjectUtils.isNotEmpty(userWorkflows)) {
            String workflowString = userWorkflows.stream()
                    .map(Workflow::getName)
                    .collect(Collectors.joining(", "));
            LOGGER.info("Loaded user workflows: {}", workflowString);
        } else {
            LOGGER.warn("Unable to load workflows for user: {}", authentication.getName());
        }
        response.sendRedirect("/");
    }
}

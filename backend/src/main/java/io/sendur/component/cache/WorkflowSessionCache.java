package io.sendur.component.cache;

import io.sendur.domain.workflow.Workflow;
import io.sendur.service.impl.WorkflowService;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.sendur.component.authentication.CustomOAuth2SuccessHandler;

import java.util.List;

/**
 * The {@code WorkflowSessionCache} has the responsibility of caching user workflows. The purpose:
 * <ul>
 *     <li>Limit and reduce database queries for stored workflows</li>
 *     <li>Workflows are available for manipulation and analysis on request</li>
 *     <li>When workflows are created, updated, or deleted, the application/storage has the most upto date workflows</li>
 * </ul>
 * Workflows are uploaded on successful signin and lives for the lifetime of user session. The {@code WorkflowSessionCache}
 * should not be used directly and instead used through a service layer. Some methods such as
 * {@link WorkflowService#refresh()} are not available in this class, but should be accessed through the service layer.
 *
 * @see WorkflowService
 * @see CustomOAuth2SuccessHandler
 */
// TODO: move to package with WorkflowService to protect this class
@Component
public class WorkflowSessionCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowSessionCache.class);

    private static final String WORKFLOWS_KEY = "SESSION_WORKFLOWS";

    private final HttpSession httpSession;

    @Autowired
    public WorkflowSessionCache(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public boolean add(Workflow workflow) {
        List<Workflow> currentWorkflows = get();
        int initialSize = currentWorkflows.size();
        currentWorkflows.add(workflow);
        try {
            set(currentWorkflows);
        } catch (Exception e) {
            LOGGER.error("Error adding workflow '{}' to workflow cache: {}",
                    workflow.getName(), e.getMessage());
            if (get().size() == initialSize || get().isEmpty()) {
                set(currentWorkflows);
                int currentWorkflowSize = get().size();
                return currentWorkflowSize > initialSize;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public List<Workflow> get() {
        return (List<Workflow>) httpSession.getAttribute(WORKFLOWS_KEY);
    }

    public void set(List<Workflow> workflows) {
        httpSession.setAttribute(WORKFLOWS_KEY, workflows);
    }

    public void clear() {
        httpSession.removeAttribute(WORKFLOWS_KEY);
    }

    public boolean isCached() {
        return ObjectUtils.isNotEmpty(httpSession.getAttribute(WORKFLOWS_KEY));
    }
}

package io.sendur.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ResourceLoaderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoaderService.class);

    private static final String AI_AGENT_FILE_PREFIX = "AI_Agent___";
    private static final String AI_AGENT_RAW = "AI Agent - ";
    private static final String WORKFLOW_DIR = "workflows";

    private static final String JSON_FILE_EXT = ".json";

    public ResourceLoaderService() {}

    /**
     * Load and return n8n workflow json configuration.
     *
     * <ul> Notes
     *     <li>Searches on workflow names that omit {@code AI Agent -} are still found
     *     (ex: AI Agent - Scheduled_workflow (or) Scheduled workflow are the same)
     *     </li>
     *     <li>Searches on workflow names that omit {@code AI_Agent___} are still found
     *     (ex: AI_Agent___Scheduled_workflow (or) Scheduled workflow are the same)
     *     </li>
     *     <li>Searches on workflow names that omit the file extension are the same
     *     (ex: Scheduled_workflow.json (or) AI_Agent___Scheduled_workflow.json (or) Scheduled_workflow are the same)
     *     </li>
     *     <li>The prefix is case IN_SENSITIVE (AI Agent - Scheduled_workflow and ai agent - Scheduled_workflow) are the same</li>
     * </ul>
     *
     * @param workFlowName n8n workflow name
     *
     * @return {@link String} workflow json
     */
    public String loadWorkflowJson(String workFlowName) {
        if (StringUtils.startsWithIgnoreCase(workFlowName, AI_AGENT_RAW) || StringUtils.startsWithIgnoreCase(workFlowName, AI_AGENT_FILE_PREFIX)) {
            workFlowName = StringUtils.substring(workFlowName, AI_AGENT_RAW.length()); // raw and prefix are same length
        }
        if (StringUtils.endsWithIgnoreCase(workFlowName, JSON_FILE_EXT)) { // cut off .json
            workFlowName = StringUtils.substringBeforeLast(workFlowName, JSON_FILE_EXT);
        }
        String path = Paths.get(WORKFLOW_DIR, AI_AGENT_FILE_PREFIX + workFlowName + JSON_FILE_EXT).toString();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                LOGGER.warn("Could not find workflow json file {}, From search: {}", path, workFlowName);
                return StringUtils.EMPTY;
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Cannot load workflow json: {}. {}", workFlowName, e.getMessage());
            return StringUtils.EMPTY;
        }
    }

    /**
     * Get all workflow names that are prefixed with {@code AI_AGENT___} which is the
     * current standard for AI workflow files. These files are truncated to only
     * return the base name, removing the prefix and file extension.
     *
     * @return {@link List<String>} Names of all workflows that contain AI Agents
     */
    public List<String> getAiAgentWorkFlowNames() {
        // todo: this is very naive, this should handle multiple file extensions or fail-fast
        // todo: if the workflow file is not in the correct format type (i.e. json)
        List<String> aiWorkflowNames = new ArrayList<>(); // some silly bug here
        List<String> allWorkflowNames = getAllWorkFlowNames();
        for (String workflowName : allWorkflowNames) {
            if (StringUtils.startsWithIgnoreCase(workflowName, AI_AGENT_FILE_PREFIX)) {
                workflowName = StringUtils.substring(workflowName, AI_AGENT_FILE_PREFIX.length());
                if (StringUtils.endsWithIgnoreCase(workflowName, JSON_FILE_EXT)) {
                    workflowName = StringUtils.substringBeforeLast(workflowName, JSON_FILE_EXT);
                }
                aiWorkflowNames.add(workflowName);
            }
        }
        return aiWorkflowNames;
    }

    /**
     * Get all workflow names in its Raw form. No truncation is currently processed on these
     * workflow names and what is retrieved is the full workflow name as it appears in the
     * resource file system.
     *
     * @return {@link List<String>} all workflow names
     */
    public List<String> getAllWorkFlowNames() {
        // todo: this method is misleading, it says get workflow name, however
        // todo: this returns the full file name of the workflow.
        List<String> names = new ArrayList<>();
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources(WORKFLOW_DIR);
            for (URL url : Collections.list(resources)) {
                File dir = new File(url.getFile());
                if (dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            LOGGER.info("File Name: {}", file.getName());
                            names.add(file.getName());
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error loading files from resource directory: {}. {}", WORKFLOW_DIR, e.getMessage());
        }
        return names;
    }
}

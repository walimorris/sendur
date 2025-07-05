package io.sendur.services;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

@Service
public class ResourceLoaderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoaderService.class);

    private static final String AI_AGENT_FILE_PREFIX = "AI_AGENT___";
    private static final String AI_AGENT_RAW = "AI Agent - ";
    private static final String WORKFLOW_DIR = "workflows";

    private static final String JSON_FILE_EXT = ".json";

    public ResourceLoaderService() {}

    /**
     * Load and return n8n workflow json configuration.
     * TODO: Make file names case in-sensitive
     *
     * <ul> Rules
     *     <li>Ensure the work flow name matches it's stored name</li>
     *     <li>Prefix the workflow with what its known for (ex: AI Agent - Scheduled_workflow)</li>
     *     <li>The prefix can be omitted (ex: Scheduled_workflow) will be found</li>
     *     <li>The prefix is case IN_SENSITIVE (AI Agent - Scheduled_workflow and ai agent - Scheduled_workflow) are the same</li>
     *     <li>Naming convention after the prefix is currently case SENSITIVE (Schedule_workflow and scheduled_workflow) are not the same</li>
     * </ul>
     *
     * @param workFlowName n8n workflow name
     *
     * @return {@link String} workflow json
     */
    public String loadWorkflowJson(String workFlowName) {
        if (StringUtils.startsWithIgnoreCase(workFlowName, AI_AGENT_RAW)) {
            workFlowName = workFlowName.substring(AI_AGENT_RAW.length());
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
}

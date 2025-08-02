package io.sendur.domain.workflow;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code WorkFlowPrompts} are built objects for workflow prompt configurations with the main intent
 * of allowing user admin to update the current workflow prompt text given to the n8n Ai Agent.
 */
@Getter
@Setter
public class WorkFlowPrompt {
    private String id;
    private String name;
    private String prompt;
}

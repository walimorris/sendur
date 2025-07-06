package io.sendur.models.workflows;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * {@code WorkFlowPrompts} are built objects for workflow prompt configurations with the main intent
 * of allowing user admin to update the current workflow prompt text given to the n8n Ai Agent.
 */
@Getter
@Setter
public class WorkFlowPrompt {
    private UUID id;
    private String name;
    private String prompt;
}

package io.sendur.domain.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Meta {
    private String templateID;
    private boolean templateCredsSetupCompleted;
    private String instanceID;

    @JsonProperty("templateId")
    public String getTemplateID() { return templateID; }

    @JsonProperty("templateId")
    public void setTemplateID(String value) { this.templateID = value; }

    @JsonProperty("templateCredsSetupCompleted")
    public boolean getTemplateCredsSetupCompleted() { return templateCredsSetupCompleted; }

    @JsonProperty("templateCredsSetupCompleted")
    public void setTemplateCredsSetupCompleted(boolean value) { this.templateCredsSetupCompleted = value; }

    @JsonProperty("instanceId")
    public String getInstanceID() { return instanceID; }

    @JsonProperty("instanceId")
    public void setInstanceID(String value) { this.instanceID = value; }
}

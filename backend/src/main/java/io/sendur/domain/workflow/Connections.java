package io.sendur.domain.workflow;

import com.fasterxml.jackson.annotation.*;

public class Connections {
    private OpenAIModel openAIModel;
    private AIAgent scheduleTrigger;
    private AIAgent aiAgent;
    private SerpAPI serpAPI;
    private AIAgent getAllLeads;
    private AIAgent blockedLeads;
    private AIAgent structureTheLeads;

    @JsonProperty("OpenAI Model")
    public OpenAIModel getOpenAIModel() { return openAIModel; }

    @JsonProperty("OpenAI Model")
    public void setOpenAIModel(OpenAIModel value) { this.openAIModel = value; }

    @JsonProperty("Schedule Trigger")
    public AIAgent getScheduleTrigger() { return scheduleTrigger; }

    @JsonProperty("Schedule Trigger")
    public void setScheduleTrigger(AIAgent value) { this.scheduleTrigger = value; }

    @JsonProperty("AI Agent")
    public AIAgent getAIAgent() { return aiAgent; }

    @JsonProperty("AI Agent")
    public void setAIAgent(AIAgent value) { this.aiAgent = value; }

    @JsonProperty("SerpAPI")
    public SerpAPI getSerpAPI() { return serpAPI; }

    @JsonProperty("SerpAPI")
    public void setSerpAPI(SerpAPI value) { this.serpAPI = value; }

    @JsonProperty("Get All Leads")
    public AIAgent getGetAllLeads() { return getAllLeads; }

    @JsonProperty("Get All Leads")
    public void setGetAllLeads(AIAgent value) { this.getAllLeads = value; }

    @JsonProperty("Blocked Leads")
    public AIAgent getBlockedLeads() { return blockedLeads; }

    @JsonProperty("Blocked Leads")
    public void setBlockedLeads(AIAgent value) { this.blockedLeads = value; }

    @JsonProperty("Structure the Leads")
    public AIAgent getStructureTheLeads() { return structureTheLeads; }

    @JsonProperty("Structure the Leads")
    public void setStructureTheLeads(AIAgent value) { this.structureTheLeads = value; }
}

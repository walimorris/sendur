package io.sendur.models.workflows;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SerpAPI {
    private List<List<Main>> aiTool;

    @JsonProperty("ai_tool")
    public List<List<Main>> getAITool() { return aiTool; }

    @JsonProperty("ai_tool")
    public void setAITool(List<List<Main>> value) { this.aiTool = value; }
}

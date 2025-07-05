package io.sendur.models.workflows;

import com.fasterxml.jackson.annotation.*;
import java.util.List;

public class OpenAIModel {
    private List<List<Main>> aiLanguageModel;

    @JsonProperty("ai_languageModel")
    public List<List<Main>> getAILanguageModel() { return aiLanguageModel; }

    @JsonProperty("ai_languageModel")
    public void setAILanguageModel(List<List<Main>> value) { this.aiLanguageModel = value; }
}

package io.sendur.domain.workflow;

import com.fasterxml.jackson.annotation.*;

public class Credentials {
    private API openAIAPI;
    private API serpAPI;
    private API oAuth2API;

    @JsonProperty("openAiApi")
    public API getOpenAIAPI() { return openAIAPI; }

    @JsonProperty("openAiApi")
    public void setOpenAIAPI(API value) { this.openAIAPI = value; }

    @JsonProperty("serpApi")
    public API getSerpAPI() { return serpAPI; }

    @JsonProperty("serpApi")
    public void setSerpAPI(API value) { this.serpAPI = value; }

    @JsonProperty("oAuth2Api")
    public API getOAuth2API() { return oAuth2API; }

    @JsonProperty("oAuth2Api")
    public void setOAuth2API(API value) { this.oAuth2API = value; }
}

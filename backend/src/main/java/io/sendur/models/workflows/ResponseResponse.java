package io.sendur.models.workflows;

import com.fasterxml.jackson.annotation.*;

public class ResponseResponse {
    private boolean fullResponse;
    private String responseFormat;

    @JsonProperty("fullResponse")
    public boolean getFullResponse() { return fullResponse; }

    @JsonProperty("fullResponse")
    public void setFullResponse(boolean value) { this.fullResponse = value; }

    @JsonProperty("responseFormat")
    public String getResponseFormat() { return responseFormat; }

    @JsonProperty("responseFormat")
    public void setResponseFormat(String value) { this.responseFormat = value; }
}

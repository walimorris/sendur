package io.sendur.domain.workflow;

import com.fasterxml.jackson.annotation.*;

public class OptionsResponse {
    private ResponseResponse response;

    @JsonProperty("response")
    public ResponseResponse getResponse() { return response; }

    @JsonProperty("response")
    public void setResponse(ResponseResponse value) { this.response = value; }
}

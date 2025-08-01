package io.sendur.domain.workflow;

import com.fasterxml.jackson.annotation.*;

public class Options {
    private String gl;
    private String hl;
    private Batching batching;
    private OptionsResponse response;

    @JsonProperty("gl")
    public String getGl() { return gl; }

    @JsonProperty("gl")
    public void setGl(String value) { this.gl = value; }

    @JsonProperty("hl")
    public String getHl() { return hl; }

    @JsonProperty("hl")
    public void setHl(String value) { this.hl = value; }

    @JsonProperty("batching")
    public Batching getBatching() { return batching; }

    @JsonProperty("batching")
    public void setBatching(Batching batching) { this.batching = batching; }

    @JsonProperty("response")
    public OptionsResponse getResponse() { return response; }

    @JsonProperty("response")
    public void setResponse(OptionsResponse value) { this.response = value; }
}

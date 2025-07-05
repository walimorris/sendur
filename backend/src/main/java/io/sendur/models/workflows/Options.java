package io.sendur.models.workflows;

import com.fasterxml.jackson.annotation.*;

public class Options {
    private String gl;
    private String hl;
    private OptionsResponse response;

    @JsonProperty("gl")
    public String getGl() { return gl; }

    @JsonProperty("gl")
    public void setGl(String value) { this.gl = value; }

    @JsonProperty("hl")
    public String getHl() { return hl; }

    @JsonProperty("hl")
    public void setHl(String value) { this.hl = value; }

    @JsonProperty("response")
    public OptionsResponse getResponse() { return response; }

    @JsonProperty("response")
    public void setResponse(OptionsResponse value) { this.response = value; }
}

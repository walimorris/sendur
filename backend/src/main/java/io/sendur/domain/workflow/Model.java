package io.sendur.domain.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Model {
    private boolean __rl;
    private String mode;
    private String value;

    @JsonProperty("__rl")
    public boolean isRl() { return __rl; }

    @JsonProperty("__rl")
    public void setRl(boolean rl) { this.__rl = rl; }

    @JsonProperty("mode")
    public String getMode() { return mode; }

    @JsonProperty("mode")
    public void setMode(String mode) { this.mode = mode; }

    @JsonProperty("value")
    public String getValue() { return value; }

    @JsonProperty("value")
    public void setValue(String value) { this.value = value; }
}

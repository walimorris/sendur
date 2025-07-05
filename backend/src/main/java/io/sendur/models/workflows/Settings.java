package io.sendur.models.workflows;

import com.fasterxml.jackson.annotation.*;

public class Settings {
    private String executionOrder;

    @JsonProperty("executionOrder")
    public String getExecutionOrder() { return executionOrder; }

    @JsonProperty("executionOrder")
    public void setExecutionOrder(String value) { this.executionOrder = value; }
}

package io.sendur.models.workflows;

import com.fasterxml.jackson.annotation.*;

public class Main {
    private String node;
    private String type;
    private long index;

    @JsonProperty("node")
    public String getNode() { return node; }

    @JsonProperty("node")
    public void setNode(String value) { this.node = value; }

    @JsonProperty("type")
    public String getType() { return type; }

    @JsonProperty("type")
    public void setType(String value) { this.type = value; }

    @JsonProperty("index")
    public long getIndex() { return index; }

    @JsonProperty("index")
    public void setIndex(long value) { this.index = value; }
}

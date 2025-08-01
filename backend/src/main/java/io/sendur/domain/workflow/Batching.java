package io.sendur.domain.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Batching {
    private int batchSize;

    @JsonProperty("batchSize")
    public int getBatchSize() { return batchSize; }

    @JsonProperty("batchSize")
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
}

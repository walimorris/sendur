package io.sendur.domain.workflow;

import com.fasterxml.jackson.annotation.*;

public class Interval {
    private long triggerAtHour;

    @JsonProperty("triggerAtHour")
    public long getTriggerAtHour() { return triggerAtHour; }

    @JsonProperty("triggerAtHour")
    public void setTriggerAtHour(long value) { this.triggerAtHour = value; }
}

package io.sendur.domain.workflow;

import com.fasterxml.jackson.annotation.*;
import java.util.List;

public class Rule {
    private List<Interval> interval;

    @JsonProperty("interval")
    public List<Interval> getInterval() { return interval; }

    @JsonProperty("interval")
    public void setInterval(List<Interval> value) { this.interval = value; }
}

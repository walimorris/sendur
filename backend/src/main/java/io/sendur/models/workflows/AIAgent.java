package io.sendur.models.workflows;

import com.fasterxml.jackson.annotation.*;
import java.util.List;

public class AIAgent {
    private List<List<Main>> main;

    @JsonProperty("main")
    public List<List<Main>> getMain() { return main; }

    @JsonProperty("main")
    public void setMain(List<List<Main>> value) { this.main = value; }
}

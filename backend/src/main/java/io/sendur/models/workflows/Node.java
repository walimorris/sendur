package io.sendur.models.workflows;

import com.fasterxml.jackson.annotation.*;
import java.util.List;
import java.util.UUID;

public class Node {
    private Parameters parameters;
    private UUID id;
    private String name;
    private String type;
    private double typeVersion;
    private List<Long> position;
    private Credentials credentials;

    @JsonProperty("parameters")
    public Parameters getParameters() { return parameters; }

    @JsonProperty("parameters")
    public void setParameters(Parameters value) { this.parameters = value; }

    @JsonProperty("id")
    public UUID getID() { return id; }

    @JsonProperty("id")
    public void setID(UUID value) { this.id = value; }

    @JsonProperty("name")
    public String getName() { return name; }

    @JsonProperty("name")
    public void setName(String value) { this.name = value; }

    @JsonProperty("type")
    public String getType() { return type; }

    @JsonProperty("type")
    public void setType(String value) { this.type = value; }

    @JsonProperty("typeVersion")
    public double getTypeVersion() { return typeVersion; }

    @JsonProperty("typeVersion")
    public void setTypeVersion(double value) { this.typeVersion = value; }

    @JsonProperty("position")
    public List<Long> getPosition() { return position; }

    @JsonProperty("position")
    public void setPosition(List<Long> value) { this.position = value; }

    @JsonProperty("credentials")
    public Credentials getCredentials() { return credentials; }

    @JsonProperty("credentials")
    public void setCredentials(Credentials value) { this.credentials = value; }
}

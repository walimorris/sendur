package io.sendur.domain.workflow;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.UUID;

@Data
@Document("workflows")
public class Workflow {

    /**
     * The business level id field (the workflow id) is separate from the
     * storage level _id field (mongodb). JsonIgnoreProperties ensures that
     * _id is ignored during deserialization (uploads) but is read during
     * serialization for responses. Avoids including null _id in Json with
     * the JsonInclude. All other annotations are self-explanatory. The
     * property should be read as '_id' it should be serialized as string.
     */
    @JsonProperty("_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(value = { "_id" }, allowGetters = true)
    @Id
    @BsonId
    @Field("_id")
    private ObjectId _id;

    private String name;
    private List<Node> nodes;
    private PinData pinData;
    private Connections connections;
    private boolean active;
    private Settings settings;
    private UUID versionID;
    private Meta meta;
    private String id;
    private List<Object> tags;

    @JsonProperty("name")
    public String getName() { return name; }

    @JsonProperty("name")
    public void setName(String value) { this.name = value; }

    @JsonProperty("nodes")
    public List<Node> getNodes() { return nodes; }

    @JsonProperty("nodes")
    public void setNodes(List<Node> value) { this.nodes = value; }

    @JsonProperty("pinData")
    public PinData getPinData() { return pinData; }

    @JsonProperty("pinData")
    public void setPinData(PinData value) { this.pinData = value; }

    @JsonProperty("connections")
    public Connections getConnections() { return connections; }

    @JsonProperty("connections")
    public void setConnections(Connections value) { this.connections = value; }

    @JsonProperty("active")
    public boolean getActive() { return active; }

    @JsonProperty("active")
    public void setActive(boolean value) { this.active = value; }

    @JsonProperty("settings")
    public Settings getSettings() { return settings; }

    @JsonProperty("settings")
    public void setSettings(Settings value) { this.settings = value; }

    @JsonProperty("versionId")
    public UUID getVersionID() { return versionID; }

    @JsonProperty("versionId")
    public void setVersionID(UUID value) { this.versionID = value; }

    @JsonProperty("meta")
    public Meta getMeta() { return meta; }

    @JsonProperty("meta")
    public void setMeta(Meta value) { this.meta = value; }

    @JsonProperty("id")
    public String getID() { return id; }

    @JsonProperty("id")
    public void setID(String value) { this.id = value; }

    @JsonProperty("tags")
    public List<Object> getTags() { return tags; }

    @JsonProperty("tags")
    public void setTags(List<Object> value) { this.tags = value; }
}

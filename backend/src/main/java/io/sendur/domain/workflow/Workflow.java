package io.sendur.domain.workflow;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
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

    // workflow id (business) - different from _id for persistence
    @JsonProperty("id")
    private String workflowId;
    private String name;
    private List<Map<String, Object>> nodes;
    private Map<String, Object> pinData;
    private Map<String, Object> connections;
    private boolean active;
    private Map<String, Object> settings;
    private UUID versionId;
    private Map<String, Object> meta;
    private List<Object> tags;
}

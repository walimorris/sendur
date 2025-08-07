package io.sendur.domain.execution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Map;

@Data
@Getter
@Setter
@Document("executions")
public class Execution {

    @JsonProperty("_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(value = { "_id" }, allowGetters = true)
    @BsonId
    @Field("_id")
    private Long _id;

    // execution id (business) - different from _id for persistence
    @JsonProperty("id")
    private Long executionId;
    private Boolean finished;
    private String mode;
    private Long retryOf;
    private Long retrySuccessId;
    private Instant startedAt;
    private Instant stoppedAt;
    private String workflowId;
    private Instant waitTill;
    private Map<String, Object> customData;
}

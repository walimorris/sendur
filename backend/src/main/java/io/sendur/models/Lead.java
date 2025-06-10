package io.sendur.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document("leads")
public class Lead {

    @Id
    @BsonId
    @JsonProperty("_id")
    @Field("_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

    private String businessName;
    private String phone;
    private String email;
    private String city;
    private String website;
    private String emailDraft;
    private boolean haveContacted;

    public Lead() {}

    public Lead(Builder builder) {
        this.id = builder.id;
        this.businessName = builder.businessName;
        this.phone = builder.phone;
        this.email = builder.email;
        this.city = builder.city;
        this.website = builder.website;
        this.emailDraft = builder.emailDraft;
        this.haveContacted = builder.haveContacted;
    }

    public static class Builder {
        private ObjectId id;
        private String businessName;
        private String phone;
        private String email;
        private String city;
        private String website;
        private String emailDraft;
        private boolean haveContacted;

        public Builder() {
            // nothing to complete inside builder
        }

        public Builder id(ObjectId id) {
            this.id = id;
            return this;
        }

        public Builder businessName(String businessName) {
            this.businessName = businessName;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder emailDraft(String emailDraft) {
            this.emailDraft = emailDraft;
            return this;
        }

        public Builder haveContacted(boolean haveContacted) {
            this.haveContacted = haveContacted;
            return this;
        }

        public Lead build() {
            return new Lead(this);
        }
    }
}

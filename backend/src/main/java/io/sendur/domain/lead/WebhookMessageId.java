package io.sendur.domain.lead;

import lombok.Data;

@Data
public class WebhookMessageId {
    private String messageId;

    public WebhookMessageId() {}

    public WebhookMessageId(String messageId) {
        this.messageId = messageId;
    }
}

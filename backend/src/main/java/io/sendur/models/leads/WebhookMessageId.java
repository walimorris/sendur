package io.sendur.models.leads;

import lombok.Data;

@Data
public class WebhookMessageId {
    private String messageId;

    public WebhookMessageId(String messageId) {
        this.messageId = messageId;
    }
}

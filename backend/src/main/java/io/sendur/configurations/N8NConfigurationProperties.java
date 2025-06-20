package io.sendur.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "n8n")
public class N8NConfigurationProperties {
    private String approvedEmailsWebhook;
    private String sendGridApiKey;
    private long timeout;
    private String host;
    private int port;
}
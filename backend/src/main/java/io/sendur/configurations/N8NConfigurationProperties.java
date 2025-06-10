package io.sendur.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "n8n")
public class N8NConfigurationProperties {
    private String n8nApprovedEmailsWebhook;
    private long n8nTimeout;
}
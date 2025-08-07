package io.sendur.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@Getter
@Setter
@ConfigurationProperties(prefix = "n8n")
public class N8NConfigurationProperties {

    /**
     * The reader client is the {@linkplain AuthenticationPrincipal principal} from cognito
     * that provides n8n with authorization scope when authenticated with {@link Jwt}.
     */
    private String readerClient;

    /**
     * The reader-writer client is the {@linkplain AuthenticationPrincipal principal} from cognito
     * that provides n8n with authorization scope when authenticated with {@link Jwt}.
     */
    private String readerWriterClient;

    /**
     * The n8n workflow url that contains a webhook that kicks of the email approval process.
     */
    private String approvedEmailsWebhook;

    /**
     * The n8n instance execution api endpoint.
     */
    private String executionsEndpoint;

    /**
     * API key for n8n programmatic access.
     */
    private String apiKey;

    /**
     * API key for send grid.
     */
    private String sendGridApiKey;

    /**
     * Timeout for n8n requests.
     */
    private long timeout;

    /**
     * n8n server host.
     */
    private String host;

    /**
     * n8n server port.
     */
    private int port;
}
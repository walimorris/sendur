package io.sendur.health.impl;

import io.sendur.configuration.N8NConfigurationProperties;
import io.sendur.health.BaseHealthIndicator;
import io.sendur.service.impl.N8NGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
public class N8NHealthIndicator implements BaseHealthIndicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(N8NHealthIndicator.class);

    private final N8NConfigurationProperties n8NConfigurationProperties;
    private final N8NGatewayService n8NGatewayService;

    @Autowired
    public N8NHealthIndicator(N8NConfigurationProperties n8NConfigurationProperties, N8NGatewayService n8NGatewayService) {
        this.n8NConfigurationProperties = n8NConfigurationProperties;
        this.n8NGatewayService = n8NGatewayService;
    }

    @Override
    public Health health() {

        final String host = n8NConfigurationProperties.getHost();
        final String port = n8NConfigurationProperties.getHost();
        if (n8NGatewayService.agentSocketAccepting()) {
            return Health.up()
                    .withDetail("Host", host)
                    .withDetail("Port", port)
                    .build();
        }
        LOGGER.info("N8N PING FAILURE, host: {}, port: {}", host, port);
        return Health.down()
                .withDetail("Host", host)
                .withDetail("Port", port)
                .withDetail("N8NWebError", "N8N server down")
                .build();
    }
}

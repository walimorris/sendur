package io.sendur;

import io.sendur.configurations.N8NConfigurationProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(N8NConfigurationProperties.class)
public class SiteApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(SiteApplication.class);

	@Autowired
	private N8NConfigurationProperties n8nProperties;

	public static void main(String[] args) {
		SpringApplication.run(SiteApplication.class, args);
	}

	@PostConstruct
	public void init() {
		LOGGER.info("Webhook: {}", n8nProperties.getApprovedEmailsWebhook());
		LOGGER.info("Timeout: {}", n8nProperties.getTimeout());
	}
}

package io.sendur.services;

import io.sendur.configurations.N8NConfigurationProperties;
import io.sendur.models.Lead;
import io.sendur.repositories.LeadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class N8NService {
    private static final Logger LOGGER = LoggerFactory.getLogger(N8NService.class);


    private final LeadRepository leadRepository;
    private final N8NConfigurationProperties n8NConfigurationProperties;

    @Autowired
    public N8NService(LeadRepository leadRepository, N8NConfigurationProperties n8NConfigurationProperties) {
        this.leadRepository = leadRepository;
        this.n8NConfigurationProperties = n8NConfigurationProperties;
    }

    public HttpResponse<String> sendApprovedEmailsToLeads(List<Lead> leads) {
        try {
            HttpResponse<String> response = hitN8NApprovedEmailWebhook(leads);
            if (response.statusCode() == 200) {
                leadRepository.saveAll(leads);
                return response;
            }
            return response;
        } catch (Exception e) {
            LOGGER.error("Failed to save and send approved leads to AI Agent: {}", e.getMessage());
            return null;
        }
    }

    private HttpResponse<String> hitN8NApprovedEmailWebhook(List<Lead> leads) {
        return postN8NWebhook(n8NConfigurationProperties.getN8nApprovedEmailsWebhook(),
                n8NConfigurationProperties.getN8nTimeout(), leads);
    }

    private HttpResponse<String> postN8NWebhook(String webhook, long timeout, Object object) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhook))
                .header("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                .POST(HttpRequest.BodyPublishers.ofString(object.toString()))
                .timeout(Duration.ofSeconds(timeout))
                .build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to send POST request to N8N webhook {}: {}", webhook, e.getMessage());
            return null;
        }
    }
}

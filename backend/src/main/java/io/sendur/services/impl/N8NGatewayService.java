package io.sendur.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sendur.configurations.N8NConfigurationProperties;
import io.sendur.models.leads.ApprovedLeadsWebhookResult;
import io.sendur.models.leads.Lead;
import io.sendur.models.leads.WebhookMessageId;
import io.sendur.repositories.LeadRepository;
import io.sendur.services.N8NGateway;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class N8NGatewayService implements N8NGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(N8NGatewayService.class);

    private final N8NConfigurationProperties n8NConfigurationProperties;

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String USER_AGENT = "User-Agent";
    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String APP_NAME = "Sendur";

    @Autowired
    public N8NGatewayService(N8NConfigurationProperties n8NConfigurationProperties) {
        this.n8NConfigurationProperties = n8NConfigurationProperties;
    }

    @Override
    public boolean n8nSocketAccepting() throws IllegalStateException {
        final String host = n8NConfigurationProperties.getHost();
        final int port = n8NConfigurationProperties.getPort();
        try (Socket socket = new Socket()) {
            SocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address, (int) n8NConfigurationProperties.getTimeout());
            if (socket.isConnected()) {
                LOGGER.info("n8n HOST '{}' on PORT '{}' is open and accepting", host, port);
                return true;
            } else {
                throw new IllegalStateException("n8n HOST " + host + " on PORT " + port + " is closed and not accepting");
            }
        } catch (IOException e) {
            LOGGER.error("Can't connect to {}:{}: {}", host, port, e.getMessage());
        }
        return false;
    }

    @Override
    public ClassicHttpResponse postN8NWebhook(String webhook, long timeout, Object object) throws JsonProcessingException {
        String json = new ObjectMapper().writeValueAsString(object);
        RequestConfig config = RequestConfig.custom()
                .setResponseTimeout(timeout, TimeUnit.SECONDS)
                .build();
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build()) {
            HttpPost post = new HttpPost(webhook);
            post.setHeader(CONTENT_TYPE, APPLICATION_JSON);
            post.setHeader(USER_AGENT, APP_NAME);
            post.setEntity(new StringEntity(json));

            if (n8nSocketAccepting()) {
                return client.execute(post);
            }
        } catch (IOException | IllegalStateException e) {
            LOGGER.error("Failed to send POST request to N8N webhook {}: {}", webhook, e.getMessage());
        }
        return new BasicClassicHttpResponse(500, "Internal Server Error");
    }

    @Override
    public ApprovedLeadsWebhookResult sendApprovedEmailsToLeads(LeadRepository leadRepository, List<Lead> leads) {
        try (ClassicHttpResponse response = hitN8NApprovedEmailWebhook(leads)) {
            int statusCode = response.getCode();
            String content = getEntityContentOrDefault(response.getEntity(), "");
            ObjectMapper mapper = new ObjectMapper();
            List<WebhookMessageId> webhookMessageIdList;
            if (StringUtils.isEmpty(content)) {
                webhookMessageIdList = new ArrayList<>();
            } else {
                webhookMessageIdList = mapper.readValue(content, new TypeReference<>() {});
            }
            if (statusCode == 200) {
                leadRepository.saveAll(leads);
            }
            return new ApprovedLeadsWebhookResult(statusCode, webhookMessageIdList);
        } catch (Exception e) {
            LOGGER.error("Failed to send and save approved leads: {}", e.getMessage(), e);
            return new ApprovedLeadsWebhookResult(500, null);
        }
    }

    /**
     * Sends {@linkplain Lead leads} to n8n approved email webhook.
     *
     * @param leads approved leads
     *
     * @return {@linkplain ClassicHttpResponse HttpResponse}
     *
     * @throws JsonProcessingException leads are malformed
     */
    private ClassicHttpResponse hitN8NApprovedEmailWebhook(List<Lead> leads) throws JsonProcessingException {
        return postN8NWebhook(n8NConfigurationProperties.getApprovedEmailsWebhook(),
                n8NConfigurationProperties.getTimeout(), leads);
    }

    public String getEntityContentOrDefault(HttpEntity entity, String defaultContent) {
        if (ObjectUtils.isEmpty(entity)) {
            return defaultContent;
        }
        try {
            return entity.getContent().toString();
        } catch (IOException e) {
            LOGGER.error("Error fetching Entity Content, returning default: {}", e.getMessage());
            return defaultContent;
        }
    }
}

package io.sendur.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sendur.configuration.N8NConfigurationProperties;
import io.sendur.domain.execution.Execution;
import io.sendur.domain.execution.Executions;
import io.sendur.domain.execution.ExecutionsResult;
import io.sendur.domain.lead.ApprovedLeadsWebhookResult;
import io.sendur.domain.lead.Lead;
import io.sendur.domain.lead.WebhookMessageId;
import io.sendur.repository.LeadRepository;
import io.sendur.service.AIAgentPlatformGateway;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.brotli.BrotliInterceptor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class N8NGatewayService implements AIAgentPlatformGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(N8NGatewayService.class);

    private final N8NConfigurationProperties n8NConfigurationProperties;

    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String USER_AGENT = "User-Agent";
    private static final String N8N_API_KEY_HEADER = "X-N8N-API-KEY";
    private static final String APPLICATION_JSON = "application/json";
    private static final String SERVER_ERROR = "Internal Server Error";
    private static final String APP_NAME = "Sendur";

    private static final String EXECUTIONS_ENDPOINT_FAILURE_MSG_1 = "Failed to get executions from endpoint";
    private static final String EXECUTIONS_ENDPOINT_FAILURE_MSG_2 = "Failed to hit executions endpoint. Ensure" +
            " n8n server is running or review api-key.";
    private static final String NO_EXECUTIONS = "No Executions Available.";
    private static final String UNKNOWN_ERROR = "Unknown error";
    private static final String SOCKET_NOT_ACCEPTING_MSG = "n8n socket not accepting connections";

    private final ObjectMapper objectMapper;

    public N8NGatewayService(N8NConfigurationProperties n8NConfigurationProperties, ObjectMapper objectMapper) {
        this.n8NConfigurationProperties = n8NConfigurationProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean agentSocketAccepting() throws IllegalStateException {
        final String host = n8NConfigurationProperties.getHost();
        final int port = n8NConfigurationProperties.getPort();
        try (Socket socket = new Socket()) {
            SocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address, (int) n8NConfigurationProperties.getTimeout());
            if (socket.isConnected()) {
                LOGGER.info("n8n HOST '{}' on PORT '{}' is open and accepting", host, port);
                return true;
            } else {
                LOGGER.warn("n8n HOST: {} on PORT: {} is closed and not accepting", host, port);
                return false;
            }
        } catch (IOException e) {
            LOGGER.error("Can't connect to {}:{}: {}", host, port, e.getMessage());
        }
        return false;
    }

    @Override
    public ApprovedLeadsWebhookResult sendApprovedEmailsToLeads(LeadRepository leadRepository, List<Lead> leads) {
        try (ClassicHttpResponse response = hitN8NApprovedEmailWebhook(leads)) {
            int statusCode = response.getCode();
            String content = getEntityContentOrEmpty(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
            List<WebhookMessageId> webhookMessageIdList;
            if (StringUtils.isEmpty(content)) {
                webhookMessageIdList = new ArrayList<>();
            } else {
                InputStream webhookMessageIdListInputStream = getInputStreamFromContent(content);
                webhookMessageIdList = mapper.readValue(webhookMessageIdListInputStream, new TypeReference<>() {});
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

    @Override
    public ExecutionsResult retrieveExecutionsByWorkflowId(String workflowId) {
        String executionsEndpoint = n8NConfigurationProperties.getExecutionsEndpoint();
        executionsEndpoint = String.format("%s?workflowId=%s", executionsEndpoint, workflowId);
        return callExecutionsEndpoint(executionsEndpoint, true);
    }

    @Override
    public ExecutionsResult retrieveExecutionByExecutionId(String executionId) {
        String executionsEndpoint = n8NConfigurationProperties.getExecutionsEndpoint();
        executionsEndpoint = String.format("%s/%s?includeData=false", executionsEndpoint, executionId);
        return callExecutionsEndpoint(executionsEndpoint, false);
    }

    @Override
    public ExecutionsResult retrieveExecutionsByExecutionsIds(String... executionIds) {
        return null;
    }

    @Override
    public ExecutionsResult retrieveAllExecutions() {
        String executionsEndpoint = n8NConfigurationProperties.getExecutionsEndpoint();
        executionsEndpoint = String.format("%s?limit=20&includeData=false", executionsEndpoint);
        return callExecutionsEndpoint(executionsEndpoint, true);
    }

    protected ExecutionsResult callExecutionsEndpoint(String executionsEndpoint, boolean isMultiple) {
        String n8nApiKey = n8NConfigurationProperties.getApiKey();
        Duration timeout = Duration.ofMillis(n8NConfigurationProperties.getTimeout());
        if (StringUtils.isAnyEmpty(executionsEndpoint, n8nApiKey)) {
            return failedExecutionsRequest(executionsEndpoint, EXECUTIONS_ENDPOINT_FAILURE_MSG_2);
        }
        OkHttpClient client = okHttpClient(timeout);
        Request request = basicRequest(executionsEndpoint, n8nApiKey);
        if (!agentSocketAccepting()) {
            return socketNotAcceptingExecutionsResult();
        }
        try (Response response = client.newCall(request).execute()) {
            int status = response.code();
            if (!response.isSuccessful()) {
                return responseNotSuccessfulExecutionsResult(response, status);
            }
            String body = response.body().string();
            if (StringUtils.isEmpty(body)) {
                return new ExecutionsResult(status, new ArrayList<>(), Optional.of(NO_EXECUTIONS));
            }
            if (isMultiple) {
                return executeMultipleCursorRequests(client, executionsEndpoint, body, status, n8nApiKey);
            }
            Execution execution = objectMapper.readValue(body, new TypeReference<>(){});
            return new ExecutionsResult(status, List.of(execution), Optional.empty());
        } catch (IOException e) {
            return failedExecutionsRequest(executionsEndpoint, EXECUTIONS_ENDPOINT_FAILURE_MSG_1, e);
        }
    }

    /**
     * The n8n API responds with a {@code cursor} value. This can be seen in {@link Executions#getNextCursor()}.
     * If a cursor value is available, another request to the {@code executionsEndpoint} is created by updating
     * this cursor value and calling the next set of execution results. Executions are appended to a results list.
     * This multi-cursor request sequence continues until all executions are read. NOTE: If some malformed response
     * or empty response occurs after the initial successful request and during a subsequent multi cursor request,
     * the iteration breaks and returns the executions that have been collected. This is response is determined as
     * successful.
     *
     * @param client {@link OkHttpClient}
     * @param executionsEndpoint {@link String} the executions request endpoint
     * @param initialBody {@link String} the initial response body
     * @param initialStatus int - the initial status
     * @param key {@link String} n8n api key
     *
     * @return {@link ExecutionsResult}
     */
    protected ExecutionsResult executeMultipleCursorRequests(OkHttpClient client,
                                                           String executionsEndpoint,
                                                           String initialBody,
                                                           int initialStatus, String key) {
        try {
            HttpUrl baseHttpUrl = HttpUrl.parse(executionsEndpoint);
            Executions executions = objectMapper.readValue(initialBody, new TypeReference<>() {});
            List<Execution> allExecutions = new ArrayList<>(executions.getExecutions());
            String nextCursor = executions.getNextCursor();

            // continue to call pages while cursor is available
            while (StringUtils.isNotEmpty(nextCursor)) {
                if (ObjectUtils.isNotEmpty(baseHttpUrl)) {
                    HttpUrl pagedExecutionUrl = baseHttpUrl.newBuilder()
                            .setQueryParameter("cursor", nextCursor)
                            .build();
                    Request anotherRequest = basicRequest(pagedExecutionUrl.toString(), key);
                    try (Response anotherResponse = client.newCall(anotherRequest).execute()) {
                        String anotherBody = anotherResponse.body().string();
                        boolean anotherSuccessfulResponse = anotherResponse.isSuccessful();
                        boolean anotherValidBody = StringUtils.isNotEmpty(anotherBody);
                        if (!anotherSuccessfulResponse || !anotherValidBody) {
                            break;
                        }
                        Executions moreExecutions = objectMapper.readValue(anotherBody, new TypeReference<>(){});
                        allExecutions.addAll(moreExecutions.getExecutions());
                        nextCursor = moreExecutions.getNextCursor();
                    }
                }
            }
            // returns the initial status - if a cursor is returned but some error occurs
            // in subsequent requests then the response returns what it has as a successful
            // response
            return new ExecutionsResult(initialStatus, allExecutions, Optional.empty());
        } catch (IOException e) {
            String msg = String.format("Failed to get executions from endpoint %s ", executionsEndpoint);
            LOGGER.error("{}: {}", msg, e.getMessage());
            return new ExecutionsResult(500, new ArrayList<>(), Optional.of(msg));
        }
    }

    /**
     * Returns a basic {@code OKHttpClient} with timeout configuration.
     *
     * @param timeout {@link Duration} timeout values for: call-timeout,
     *                                connect-timeout and read-timeout
     * @return {@link OkHttpClient}
     */
    private OkHttpClient okHttpClient(Duration timeout) {
        return new OkHttpClient.Builder()
                .addInterceptor(BrotliInterceptor.INSTANCE)
                .callTimeout(timeout)
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .build();
    }

    /**
     * Returns a basic n8n executions request.
     *
     * @param executionEndpoint {@link String} executions endpoint
     * @param key {@link String} n8n api key
     *
     * @return {@link Request}
     */
    private Request basicRequest(String executionEndpoint, String key) {
        return new Request.Builder()
                .url(executionEndpoint)
                .header(ACCEPT, APPLICATION_JSON)
                .header(N8N_API_KEY_HEADER, key)
                .header(USER_AGENT, APP_NAME)
                .build();
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

    /**
     * Sends a {@linkplain HttpPost POST request} to the given n8n webhook.
     *
     * @param webhook n8n webhook target
     * @param timeout request timeout
     * @param object request body
     *
     * @return {@linkplain ClassicHttpResponse HttpResponse}
     *
     * @throws JsonProcessingException request body is malformed
     *
     * TODO: Use OKHTTP
     */
    protected ClassicHttpResponse postN8NWebhook(String webhook, long timeout, Object object) throws JsonProcessingException {
        String json = new ObjectMapper().writeValueAsString(object);
        RequestConfig config = RequestConfig.custom()
                .setResponseTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build()) {
            HttpPost post = new HttpPost(webhook);
            post.setHeader(CONTENT_TYPE, APPLICATION_JSON);
            post.setHeader(USER_AGENT, APP_NAME);
            post.setEntity(new StringEntity(json));

            if (agentSocketAccepting()) {
                return client.execute(post);
            }
        } catch (IOException | IllegalStateException e) {
            LOGGER.error("Failed to send POST request to N8N webhook {}: {}", webhook, e.getMessage());
        }
        return new BasicClassicHttpResponse(500, SERVER_ERROR);
    }

    /**
     * {@link ClassicHttpResponse} returns an {@link HttpEntity} wrapped in an {@code HttpEntityContainer}.
     * This method returns the entity's content utilizing the {@link EntityUtils}. If the Entity content is
     * empty (i.e. NULL) a default empty string is returned.
     *
     * @param entity {@link HttpEntity}
     *
     * @return {@link String} entity content or empty string if null
     */
    private String getEntityContentOrEmpty(HttpEntity entity) {
        if (ObjectUtils.isEmpty(entity)) {
            return StringUtils.EMPTY;
        }
        try {
            return EntityUtils.toString(entity);
        } catch (Exception e) {
            LOGGER.error("Error fetching Entity Content, returning default EMPTY STRING: {}", e.getMessage());
            return StringUtils.EMPTY;
        }
    }

    /**
     * Returns input stream for the given content.
     *
     * @param content {@link String} resource content
     *
     * @return {@link InputStream}
     */
    private InputStream getInputStreamFromContent(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private ExecutionsResult socketNotAcceptingExecutionsResult() {
        LOGGER.warn(SOCKET_NOT_ACCEPTING_MSG);
        return new ExecutionsResult(503, new ArrayList<>(), Optional.of(SOCKET_NOT_ACCEPTING_MSG));
    }

    private ExecutionsResult responseNotSuccessfulExecutionsResult(Response response, int status) {
        String responseBody;
        try {
            responseBody = response.body().string();
        } catch (IOException e) {
            responseBody = null;
        }
        String errorBody = StringUtils.isNotEmpty(responseBody) ? responseBody : UNKNOWN_ERROR;
        return new ExecutionsResult(status, new ArrayList<>(), Optional.of(errorBody));
    }

    private ExecutionsResult failedExecutionsRequest(String executionsEndpoint, String msg) {
        return failedExecutionsRequest(executionsEndpoint, msg, null);
    }

    private ExecutionsResult failedExecutionsRequest(String executionsEndpoint, String msg, Exception e) {
        String message = String.format(msg, " %s", executionsEndpoint);
        if (e != null) {
            LOGGER.error("{}: {}", message, e.getMessage());
        } else {
            LOGGER.error("{}", message);
        }
        return new ExecutionsResult(500, new ArrayList<>(), Optional.of(msg));
    }
}

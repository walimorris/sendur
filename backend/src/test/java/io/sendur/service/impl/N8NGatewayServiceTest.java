package io.sendur.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sendur.component.connection.SocketFactory;
import io.sendur.configuration.N8NConfigurationProperties;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class N8NGatewayServiceTest {
    private static LogCaptor logCaptor;

    @BeforeAll
    public static void setLogCaptor() {
        logCaptor = LogCaptor.forClass(N8NGatewayService.class);
    }

    @AfterEach
    public void clearLogs() {
        logCaptor.clearLogs();
    }

    @AfterAll
    public static void tearDown() {
        logCaptor.close();
    }

    @Mock
    N8NConfigurationProperties n8NConfigurationProperties;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    SocketFactory socketFactory;

    @InjectMocks
    N8NGatewayService n8NGatewayService;

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 5678;
    private static final long TIMEOUT = 5000;

    private static final String APPROVED_EMAILS_WEBHOOK = "http://127.0.0.1:5678/webhook/123456789";

    @Test
    void agentSocketAccepting_trueWhenConnected() throws IOException {
        Socket socket = Mockito.mock(Socket.class);

        when(n8NConfigurationProperties.getHost()).thenReturn(HOST);
        when(n8NConfigurationProperties.getPort()).thenReturn(PORT);
        when(n8NConfigurationProperties.getTimeout()).thenReturn(TIMEOUT);
        when(socketFactory.create()).thenReturn(socket);
        doNothing().when(socket).connect(any(SocketAddress.class), eq((int) TIMEOUT));
        when(socket.isConnected()).thenReturn(true);

        assertTrue(n8NGatewayService.agentSocketAccepting());
        verify(socket, times(1)).close();

        verify(n8NConfigurationProperties, times(1)).getHost();
        verify(n8NConfigurationProperties, times(1)).getPort();

        String log = logCaptor.getLogs().get(0);
        assertTrue(() -> log.contains(HOST));
        assertTrue(() -> log.contains(String.valueOf(PORT)));
        assertTrue(() -> log.contains("is open and accepting"));
    }

    @Test
    void agentSocketAccepting_falseWhenNotConnected() throws IOException {
        Socket socket = Mockito.mock(Socket.class);

        when(n8NConfigurationProperties.getHost()).thenReturn(HOST);
        when(n8NConfigurationProperties.getPort()).thenReturn(PORT);
        when(n8NConfigurationProperties.getTimeout()).thenReturn(TIMEOUT);
        when(socketFactory.create()).thenReturn(socket);
        doNothing().when(socket).connect(any(SocketAddress.class), eq((int) TIMEOUT));
        when(socket.isConnected()).thenReturn(false);

        assertFalse(n8NGatewayService.agentSocketAccepting());
        verify(socket, times(1)).close();

        verify(n8NConfigurationProperties, times(1)).getHost();
        verify(n8NConfigurationProperties, times(1)).getPort();

        String log = logCaptor.getLogs().get(0);
        assertTrue(() -> log.contains(HOST));
        assertTrue(() -> log.contains(String.valueOf(PORT)));
        assertTrue(() -> log.contains("is closed and not accepting"));
    }

    @Test
    void agentSocketAccepting_falseWhenExceptionIsThrown() throws IOException {
        when(n8NConfigurationProperties.getHost()).thenReturn(HOST);
        when(n8NConfigurationProperties.getPort()).thenReturn(PORT);
        when(socketFactory.create()).thenThrow(IOException.class);

        assertFalse(n8NGatewayService.agentSocketAccepting());
        verify(n8NConfigurationProperties, times(1)).getHost();
        verify(n8NConfigurationProperties, times(1)).getPort();

        String log = logCaptor.getLogs().get(0);

        assertTrue(log.contains(String.valueOf(PORT)));
        assertTrue(log.contains(HOST));
        assertTrue(log.contains("Can't connect"));
    }

    @Test
    void sendApprovedEmailsToLeads() throws JsonProcessingException {
//        ClassicHttpResponse classicHttpResponseMock = Mockito.mock(ClassicHttpResponse.class);
//        LeadRepository leadRepository = Mockito.mock(LeadRepository.class);
//        WebhookMessageId webhookMessageIdResults = new WebhookMessageId();
//        webhookMessageIdResults.setMessageId("123456789");
//
//        when(n8NConfigurationProperties.getApprovedEmailsWebhook()).thenReturn(APPROVED_EMAILS_WEBHOOK);
//        when(n8NConfigurationProperties.getTimeout()).thenReturn(TIMEOUT);
//        when(n8NGatewayService.postN8NWebhook(eq(APPROVED_EMAILS_WEBHOOK), eq(TIMEOUT), any(Object.class))).thenReturn(classicHttpResponseMock);
//        when(classicHttpResponseMock.getCode()).thenReturn(200);
//        when(n8NGatewayService.getEntityContentOrEmpty(any(HttpEntity.class))).thenReturn(webhookMessageIdResults.toString());
    }

    @Test
    void retrieveExecutionsByWorkflowId() {
    }

    @Test
    void retrieveExecutionByExecutionId() {
    }

    @Test
    void retrieveAllExecutions() {
    }

    @Test
    void retrieveExecutionsByExecutionsIds() {
    }

    @Test
    void callExecutionsEndpoint() {
    }

    @Test
    void executeMultipleCursorRequests() {
    }

    @Test
    void postN8NWebhook() {
    }
}
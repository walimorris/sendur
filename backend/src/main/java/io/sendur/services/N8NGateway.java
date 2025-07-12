package io.sendur.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.sendur.models.leads.ApprovedLeadsWebhookResult;
import io.sendur.models.leads.Lead;
import io.sendur.repositories.LeadRepository;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ClassicHttpResponse;

import java.util.List;

/**
 * {@code N8NGateway} is the Gateway Pattern implemented to provide convenient access and functionality
 * purposely meant for external n8n server communication. At the heart of {@code Sendur} is n8n running
 * multiple AI Agents and workflows, however this Gateway provides services and utilities the access to
 * n8n servers through this single gateway and simplifies communication throughout.
 */
public interface N8NGateway {

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
     */
    ClassicHttpResponse postN8NWebhook(String webhook, long timeout, Object object) throws JsonProcessingException;

    /**
     * Sends approved leads to the n8n approved emails webhook and returns the result.
     *
     * @param leads approved leads
     *
     * @return {@link ApprovedLeadsWebhookResult}
     */
    ApprovedLeadsWebhookResult sendApprovedEmailsToLeads(LeadRepository leadRepository, List<Lead> leads);

    /**
     * Determining if n8n server is accepting communication after attempting socket connection
     * from Sendur.
     *
     * @return boolean
     *
     * @throws IllegalStateException this is not wanted state
     */
    boolean n8nSocketAccepting() throws IllegalStateException;
}

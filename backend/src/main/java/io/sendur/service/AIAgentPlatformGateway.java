package io.sendur.service;

import io.sendur.domain.execution.ExecutionsResult;
import io.sendur.domain.lead.ApprovedLeadsWebhookResult;
import io.sendur.domain.lead.Lead;
import io.sendur.repository.LeadRepository;

import java.util.List;

/**
 * {@code AIAgentPlatformGateway} is the Gateway Pattern to provide convenient access and functionality
 * for external AI Agent server communication. At the heart of {@code Sendur} is n8n running multiple
 * AI Agents and workflows, however this Gateway interface is meant to be interchangeable with various
 * AI Agent platforms which provide common services and utilities to access platform servers.
 */
public interface AIAgentPlatformGateway {

    /**
     * Sends approved leads to the agent platform approved emails webhook and returns the result.
     *
     * @param leads approved leads
     *
     * @return {@link ApprovedLeadsWebhookResult}
     */
    ApprovedLeadsWebhookResult sendApprovedEmailsToLeads(LeadRepository leadRepository, List<Lead> leads);

    /**
     * Retrieve all possible n8n workflow executions.
     *
     * @return {@link ExecutionsResult}
     */
    ExecutionsResult retrieveAllExecutions();

    /**
     * Retrieve n8n workflow execution with the given executionId.
     *
     * @param executionId {@link String} executionId
     *
     * @return {@link ExecutionsResult}
     */
    ExecutionsResult retrieveExecutionByExecutionId(String executionId);

    /**
     * Retrieve all executions with the given executionIds.
     *
     * @param executionIds {@link String} executionIds
     *
     * @return {@link ExecutionsResult}
     */
    ExecutionsResult retrieveExecutionsByExecutionsIds(String... executionIds);

    /**
     * Retrieve all executions with the given workflowId.
     *
     * @param workflowId {@link String} workflowId
     *
     * @return {@link ExecutionsResult}
     */
    ExecutionsResult retrieveExecutionsByWorkflowId(String workflowId);

    /**
     * Determine if agent server is accepting communication after attempting socket connection
     * from Sendur.
     *
     * @return boolean
     *
     * @throws IllegalStateException this is not wanted state
     */
    boolean agentSocketAccepting() throws IllegalStateException;
}

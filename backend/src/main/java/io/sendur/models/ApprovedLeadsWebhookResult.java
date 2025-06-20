package io.sendur.models;

import java.util.List;

/**
 * The {@code ApprovedLeadsWebhookResult} is the result from the {@code n8n Send Approved Emails Webhook}.
 * This webhook retrieves a list of {@linkplain Lead leads}, conducts some parsing and validation logic,
 * and sends emails to the approved Leads. Each email confirmation returns a {@link WebhookMessageId}.
 * These messageIds are returned from the n8n Webhook in the form of a list of messageIds.
 *
 * @param statusCode webhook response statusCode
 * @param webhookMessageIds confirmed email receipts
 */
public record ApprovedLeadsWebhookResult(int statusCode, List<WebhookMessageId> webhookMessageIds) {}


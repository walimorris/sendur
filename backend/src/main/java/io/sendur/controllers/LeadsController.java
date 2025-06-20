package io.sendur.controllers;

import io.micrometer.common.util.StringUtils;
import io.sendur.models.*;
import io.sendur.services.LeadService;
import io.sendur.services.N8NService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/sendur/api/leads")
public class LeadsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeadsController.class);

    private final LeadService leadService;
    private final N8NService n8NService;

    @Autowired
    public LeadsController(LeadService leadService, N8NService n8NService) {
        this.leadService = leadService;
        this.n8NService = n8NService;
    }

    /**
     * Loads all available leads.
     *
     * @return {@linkplain ResponseEntity leads}
     */
    @GetMapping("/find-all")
    public ResponseEntity<List<Lead>> receiveAllLeads() {
        List<Lead> leads = leadService.loadAllLeads();
        LOGGER.info("All leads loaded: {}", leads.size());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .lastModified(Instant.now().toEpochMilli())
                .body(leads);
    }

    /**
     * Scheduled workflow every morning that runs and finds available business leads
     * based on the requirements of the N8N workflow:
     * <ol>
     *     <li>Small Businesses/Mom-Pop Shops with no website</li>
     *     <li>In certain areas</li>
     *     <li>Can update the number of results AI agent should find on
     *         the node in the prompt for OpenAI</li>
     * </ol>
     *
     * @param leads {@linkplain LeadRequest lead request}
     *
     * @return {@link ResponseEntity}
     */
    @PostMapping("/receive-scheduled-leads")
    public ResponseEntity<?> receiveScheduledLeads(@RequestBody List<LeadRequest> leads) {
        int persistedLeads = leadService.loadScheduledLeads(leads);
        LOGGER.info("Leads Persisted: {}", persistedLeads);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .lastModified(Instant.now().toEpochMilli())
                .build();
    }

    /**
     * This is called by a scheduler on N8N, which retrieves the business leads,
     * operates on them with OpenAI. The N8N workflow that calls this endpoint
     * ends by calling {@linkplain #updateLeadsWithEmails(List) /update-emails}
     * endpoint.
     *
     * @return {@linkplain ResponseEntity List of Leads}
     */
    @GetMapping("/no-email-scheduler")
    public ResponseEntity<List<Lead>> loadLeadsWithNoEmails() {
        List<Lead> leadsWithNoEmails = leadService.loadLeadsWithNoEmail();
        LOGGER.info("Current leads without email count: {}", leadsWithNoEmails.size());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .lastModified(Instant.now().toEpochMilli())
                .body(leadsWithNoEmails);
    }

    /**
     * N8N calls this endpoint after searching the web for the business lead's email
     * addresses. After that work is done, the result is posted and updated in the
     * datastore.
     *
     * @param leads {@linkplain List<Lead> List of Leads}
     *
     * @return {@link ResponseEntity}
     */
    @PostMapping("/update-emails")
    public ResponseEntity<?> updateLeadsWithEmails(@RequestBody List<Lead> leads) {
        int leadsResultSize = leadService.loadLeads(leads);
        LOGGER.info("Updating {} leads: ", leadsResultSize);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .lastModified(Instant.now().toEpochMilli())
                .build();
    }

    /**
     * This post request delivers a payload of leads, that have been approved by our admin, to our N8N
     * AI Agent who has the responsibility of sending the approved emails to our business leads.
     * <p>Here is the process:</p>
     * <ol>
     *     <li>Admin selects leads in the UI that have emails, but are yet unapproved for sending</li>
     *     <li>This list of leads are sent from frontend to this backend API</li>
     *     <li>The N8NService sends the list of approved {@linkplain Lead leads} to N8N webhook and our Agent send
     *         the emails</li>
     *     <li>If the emails to the leads are successful, The N8NService updates the approved {@linkplain Lead leads}
     *         in the datastore</li>
     *      <li>This API receives the {@link HttpResponse response} statusCode to update the admin user and UI</li>
     * </ol>
     *
     * @param leads {@linkplain List<Lead> list of leads}
     *
     * @return {@link ResponseEntity}
     */
    @PostMapping("/approve-lead-emails")
    public ResponseEntity<?> approveLeadEmails(@RequestBody List<Lead> leads) {
        LOGGER.info("Sending approved leads to N8N 'Send Approve Emails Webhook'");
        List<Lead> validatedLeads = reviewAndValidateLeadRecords(leads);
        ApprovedLeadsWebhookResult sentApprovedLeadsResponse = n8NService.sendApprovedEmailsToLeads(validatedLeads);
        if (sentApprovedLeadsResponse != null) {
            final int statusCode = sentApprovedLeadsResponse.statusCode();
            final List<WebhookMessageId> webhookMessageIdList = sentApprovedLeadsResponse.webhookMessageIds();
            LOGGER.info("success. status code: {}", sentApprovedLeadsResponse.statusCode());
            if (statusCode == 200) {
                LOGGER.info("Webhook call successful. Content: {}", webhookMessageIdList);
                return ResponseEntity.ok().body(webhookMessageIdList);
            } else {
                LOGGER.warn("Webhook call not exactly success. status code: {}", statusCode);
                return ResponseEntity.status(statusCode).body(webhookMessageIdList);
            }
        }
        LOGGER.info("something went wrong.");
        return ResponseEntity.badRequest().body("Webhook call failed");
    }

    /**
     * A validation helper to ensure lead records have filled data properties. We don't want to
     * send leads that do not have email addresses. This helps mitigate errors on the n8n webhook.
     *
     * @param leads {@linkplain Lead leads}
     *
     * @return validated {@linkplain List of leads}
     */
    private List<Lead> reviewAndValidateLeadRecords(List<Lead> leads) {
        List<Lead> validatedLeads = new ArrayList<>();
        for (Lead lead : leads) {
            if (lead != null && StringUtils.isNotEmpty(lead.getEmail())) {
                validatedLeads.add(lead);
            }
        }
        LOGGER.info("validated leads {} of {} original leads.", validatedLeads.size(), leads.size());
        return validatedLeads;
    }
}

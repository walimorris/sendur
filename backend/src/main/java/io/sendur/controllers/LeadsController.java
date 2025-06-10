package io.sendur.controllers;

import io.sendur.models.Lead;
import io.sendur.models.LeadRequest;
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
        HttpResponse<String> sentApprovedLeadsResponse = n8NService.sendApprovedEmailsToLeads(leads);
        if (sentApprovedLeadsResponse != null) {
            if (sentApprovedLeadsResponse.statusCode() == 200) {
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }
}

package io.sendur.services.impl;

import io.sendur.models.leads.Lead;
import io.sendur.models.leads.LeadRequest;
import io.sendur.repositories.LeadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeadService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeadService.class);

    private static final String LEAD_LOAD_FAILURE = "Failed to load lead: {}";
    private static final String LEADS_LOAD_MESSAGE = "Loaded {} out of {} leads";
    private static final String UPLOADING = "Uploading Lead: {}";

    private final LeadRepository leadRepository;

    @Autowired
    public LeadService(final LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    public List<Lead> loadAllLeads() {
        return leadRepository.findAll();
    }

    public List<Lead> loadLeadsWithNoEmail() {
        return leadRepository.findLeadByEmailNotAvailable();
    }

    public int loadLeads(List<Lead> leads, boolean updater) {
        int received = leads.size();
        int loaded = 0;

        if (!updater) {
            leads = deduplicateLeads(leads);
        }
        for (Lead lead : leads) {
            LOGGER.info(UPLOADING, lead.getBusinessName());
            try {
                Lead result = leadRepository.save(lead);
                if (result.getId() != null) {
                    loaded++;
                }
            } catch (Exception e) {
                leadLoadFailureLog(lead.getBusinessName());
                LOGGER.error(e.getMessage());
            }
        }
        leadsLoadedLog(loaded, received);
        return loaded;
    }

    public int loadScheduledLeads(List<LeadRequest> leads) {
        int received = leads.size();
        List<Lead> stagedLeads = new ArrayList<>();
        for (LeadRequest lead : leads) {
            Lead businessLead = new Lead.Builder()
                    .businessName(lead.getBusinessName().trim())
                    .email(lead.getEmail().trim())
                    .city(lead.getCity().trim())
                    .phone(lead.getPhone().trim())
                    .website(lead.getWebsite().trim())
                    .emailDraft(lead.getEmailDraft().trim())
                    .haveContacted(false)
                    .build();
            stagedLeads.add(businessLead);
        }
        int loaded = loadLeads(stagedLeads, false);
        leadsLoadedLog(loaded, received);
        return loaded;
    }

    private List<Lead> deduplicateLeads(List<Lead> unverifiedLeads) {
        // We do our best to filter duplicates in the OpenAI prompt, but in order to filter
        // further, let's do a last filtering here.
        List<String> persistedBusinesses = loadAllLeads().stream()
                .map(Lead::getBusinessName)
                .toList();

        // todo: test duplicates in unverifiedLeads as well
        List<Lead> verifiedLeads = new ArrayList<>();
        for (Lead unverifiedLead : unverifiedLeads) {
            String businessName = unverifiedLead.getBusinessName().trim();
            if (!persistedBusinesses.contains(businessName)) {
                verifiedLeads.add(unverifiedLead);
            }
        }
        return verifiedLeads;
    }

    private void leadLoadFailureLog(String businessName) {
        LOGGER.error(LEAD_LOAD_FAILURE, businessName);
    }

    private void leadsLoadedLog(int loaded, int received) {
        LOGGER.info(LEADS_LOAD_MESSAGE, loaded, received);
    }
}

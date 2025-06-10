package io.sendur.services;

import io.sendur.models.Lead;
import io.sendur.models.LeadRequest;
import io.sendur.repositories.LeadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeadService.class);

    private static final String LEAD_LOAD_FAILURE = "Failed to load lead: {}";
    private static final String LEADS_LOAD_MESSAGE = "Loaded {} out of {} leads";

    private final LeadRepository leadRepository;

    @Autowired
    public LeadService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    public List<Lead> loadAllLeads() {
        return leadRepository.findAll();
    }

    public List<Lead> loadLeadsWithNoEmail() {
        return leadRepository.findLeadByEmailNotAvailable();
    }

    public int loadLeads(List<Lead> leads) {
        int received = leads.size();
        int loaded = 0;
        for (Lead lead : leads) {
            System.out.println("Uploading Lead: " + lead.getBusinessName());
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
        int loaded = 0;
        for (LeadRequest lead : leads) {
            System.out.println("Uploading Lead: " + lead.getBusinessName());
            Lead businessLead = new Lead.Builder()
                    .businessName(lead.getBusinessName())
                    .email(lead.getEmail())
                    .city(lead.getCity())
                    .phone(lead.getPhone())
                    .website(lead.getWebsite())
                    .emailDraft(lead.getEmailDraft())
                    .haveContacted(false)
                    .build();
            try {
                Lead result = leadRepository.save(businessLead);
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

    private void leadLoadFailureLog(String businessName) {
        LOGGER.error(LEAD_LOAD_FAILURE, businessName);
    }

    private void leadsLoadedLog(int loaded, int received) {
        LOGGER.info(LEADS_LOAD_MESSAGE, loaded, received);
    }
}

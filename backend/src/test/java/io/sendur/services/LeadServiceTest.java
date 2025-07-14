package io.sendur.services;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sendur.TestUtils;
import io.sendur.models.leads.Lead;
import io.sendur.repositories.LeadRepository;
import io.sendur.services.impl.LeadService;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {
    private static LogCaptor logCaptor;

    private static final String PERSISTED_LEADS_RESOURCE = "leads/leads_0.json";
    private static final String UNVERIFIED_LEADS_RESOURCE_1 = "leads/leads_1.json";
    private static final String UNVERIFIED_LEADS_RESOURCE_2 = "leads/leads_2.json";

    @BeforeAll
    public static void setupLogCaptor() {
        logCaptor = LogCaptor.forClass(LeadService.class);
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
    private LeadRepository leadRepository;

    @InjectMocks
    private LeadService leadService;

    @Test
    void loadLeadsDuplicate() {
        List<Lead> persistedLeads = TestUtils.getObjectFromJsonResourceInputStream(PERSISTED_LEADS_RESOURCE, new TypeReference<>(){});
        List<Lead> unverifiedLeads = TestUtils.getObjectFromJsonResourceInputStream(UNVERIFIED_LEADS_RESOURCE_1, new TypeReference<>(){});
        int received = unverifiedLeads.size();

        when(leadRepository.findAll()).thenReturn(persistedLeads);

        int result = leadService.loadLeads(unverifiedLeads, false);

        verify(leadRepository, times(0)).save(any(Lead.class));
        assertEquals(1, received);
        assertEquals(0, result);

        assertEquals(1, logCaptor.getLogs().size());
        assertEquals("Loaded 0 out of 1 leads", logCaptor.getLogs().get(0));
    }

    @Test
    void loadLeadsUnique() {
        List<Lead> persistedLeads = TestUtils.getObjectFromJsonResourceInputStream(PERSISTED_LEADS_RESOURCE, new TypeReference<>(){});
        List<Lead> unverifiedLeads = TestUtils.getObjectFromJsonResourceInputStream(UNVERIFIED_LEADS_RESOURCE_2, new TypeReference<>(){});

        int receivedLeads = unverifiedLeads.size();

        when(leadRepository.findAll()).thenReturn(persistedLeads);
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        int result = leadService.loadLeads(unverifiedLeads, false);

        verify(leadRepository, times(2)).save(any(Lead.class));
        assertEquals(2, receivedLeads);
        assertEquals(2, result);

        assertEquals(3, logCaptor.getLogs().size());
    }

    @Test
    void loadLeadsUnverifiedDuplicate() {}

    @Test
    void loadScheduledLeads() {}
}
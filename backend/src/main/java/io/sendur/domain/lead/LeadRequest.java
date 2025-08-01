package io.sendur.domain.lead;

import lombok.Data;

@Data
public class LeadRequest {
    private String businessName;
    private String phone;
    private String email;
    private String city;
    private String website;
    private String emailDraft;
}

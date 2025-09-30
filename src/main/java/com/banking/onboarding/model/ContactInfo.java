package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfo {
    
    private String contactId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private ContactType contactType;
    private boolean isPrimary;
    
    public enum ContactType {
        PRIMARY_CONTACT,
        LEGAL_REPRESENTATIVE,
        AUTHORIZED_SIGNATORY,
        COMPLIANCE_OFFICER,
        FINANCIAL_CONTROLLER,
        OTHER
    }
}

package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityData {
    
    private String entityId;
    private String entityName;
    private EntityType entityType;
    private String registrationNumber;
    private String taxId;
    private LocalDate incorporationDate;
    private String countryOfIncorporation;
    private String businessAddress;
    private String legalAddress;
    private ContactInfo primaryContact;
    private List<ContactInfo> additionalContacts;
    private List<DocumentInfo> documents;
    private RiskProfile riskProfile;
    private ComplianceInfo complianceInfo;
    
    public enum EntityType {
        CORPORATION,
        PARTNERSHIP,
        LIMITED_LIABILITY_COMPANY,
        SOLE_PROPRIETORSHIP,
        TRUST,
        FOUNDATION,
        OTHER
    }
}

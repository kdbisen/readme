package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceInfo {
    
    private String complianceId;
    private List<RegulatoryRequirement> regulatoryRequirements;
    private List<SanctionsCheck> sanctionsChecks;
    private List<AdverseMediaCheck> adverseMediaChecks;
    private ComplianceStatus overallStatus;
    private String complianceNotes;
    private LocalDateTime lastComplianceCheck;
    private String checkedBy;
    
    public enum ComplianceStatus {
        COMPLIANT,
        NON_COMPLIANT,
        PENDING_REVIEW,
        REQUIRES_MANUAL_REVIEW
    }
    
    public enum RegulatoryRequirement {
        AML_KYC,
        FATCA,
        CRS,
        GDPR,
        PCI_DSS,
        SOX,
        OTHER
    }
    
    public enum SanctionsCheck {
        OFAC,
        EU_SANCTIONS,
        UN_SANCTIONS,
        LOCAL_SANCTIONS,
        OTHER
    }
    
    public enum AdverseMediaCheck {
        NEWS_MEDIA,
        SOCIAL_MEDIA,
        REGULATORY_NOTICES,
        COURT_RECORDS,
        OTHER
    }
}

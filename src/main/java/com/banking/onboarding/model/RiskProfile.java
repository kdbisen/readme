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
public class RiskProfile {
    
    private String riskId;
    private RiskLevel riskLevel;
    private List<RiskFactor> riskFactors;
    private String riskAssessmentNotes;
    private String riskMitigationStrategy;
    private LocalDateTime lastAssessmentDate;
    private String assessedBy;
    
    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH
    }
    
    public enum RiskFactor {
        HIGH_VOLUME_TRANSACTIONS,
        CASH_INTENSIVE_BUSINESS,
        HIGH_RISK_GEOGRAPHY,
        POLITICALLY_EXPOSED_PERSON,
        SANCTIONS_LIST_MATCH,
        COMPLEX_OWNERSHIP_STRUCTURE,
        REGULATORY_HISTORY,
        OTHER
    }
}

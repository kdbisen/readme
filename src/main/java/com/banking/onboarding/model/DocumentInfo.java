package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentInfo {
    
    private String documentId;
    private String documentName;
    private DocumentType documentType;
    private String documentPath;
    private String mimeType;
    private long fileSize;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String issuingAuthority;
    private String documentNumber;
    private DocumentStatus status;
    
    public enum DocumentType {
        CERTIFICATE_OF_INCORPORATION,
        ARTICLES_OF_ASSOCIATION,
        MEMORANDUM_OF_ASSOCIATION,
        BOARD_RESOLUTION,
        POWER_OF_ATTORNEY,
        IDENTIFICATION_DOCUMENT,
        ADDRESS_PROOF,
        FINANCIAL_STATEMENT,
        TAX_CERTIFICATE,
        OTHER
    }
    
    public enum DocumentStatus {
        PENDING,
        VERIFIED,
        REJECTED,
        EXPIRED
    }
}

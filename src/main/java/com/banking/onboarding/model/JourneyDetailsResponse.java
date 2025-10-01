package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourneyDetailsResponse {
    private String processId;
    private String correlationId;
    private String journeyId;
    private String journeyStatus;
    private String clientId;
    private String entityId;
    private List<JourneyStep> journeySteps;
    private Map<String, Object> journeyData;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
}

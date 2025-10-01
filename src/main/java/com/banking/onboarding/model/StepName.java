package com.banking.onboarding.model;

public enum StepName {
    APIGEE_TRANSFORMATION("APIGEE_TRANSFORMATION"),
    FENERGO_ENTITY_CREATE("FENERGO_ENTITY_CREATE"),
    FENERGO_JOURNEY_INFO("FENERGO_JOURNEY_INFO"),
    FENERGO_JOURNEY_INITIATE("FENERGO_JOURNEY_INITIATE"),
    FENERGO_JOURNEY_DETAILS("FENERGO_JOURNEY_DETAILS"),
    COMPLETION("COMPLETION");

    private final String value;

    StepName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

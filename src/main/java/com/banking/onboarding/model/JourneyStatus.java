package com.banking.onboarding.model;

public enum JourneyStatus {
    INITIATED("INITIATED"),
    IN_PROGRESS("IN_PROGRESS"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    PENDING("PENDING");

    private final String value;

    JourneyStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

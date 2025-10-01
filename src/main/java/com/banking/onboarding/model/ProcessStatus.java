package com.banking.onboarding.model;

public enum ProcessStatus {
    INITIATED("INITIATED"),
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    PENDING("PENDING");

    private final String value;

    ProcessStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

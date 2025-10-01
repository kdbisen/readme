package com.banking.onboarding.model;

public enum StepStatus {
    PENDING("PENDING"),
    IN_PROGRESS("IN_PROGRESS"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    RETRYING("RETRYING");

    private final String value;

    StepStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

package com.redwood.scheduler.custom.kpi.kpi3.config;

public enum JobType
{
    CONDITIONAL("Conditional"),
    AUTO("Auto"),
    SUGGESTED("Suggested"),
    RECON("Reconciliation"),
    UNKNOWN("Unknown");

    private final String displayName;

    JobType(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public String toString()
    {
        return displayName;
    }
}
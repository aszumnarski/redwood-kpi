package com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model;

import com.redwood.scheduler.api.date.DateTimeZone;

public class ScheduledCoverageEntry {

    private CoverageKey key;
    private long jobId;
    private String jobDefinition;
    private String scheduleName;
    private DateTimeZone nextRunTime;

}

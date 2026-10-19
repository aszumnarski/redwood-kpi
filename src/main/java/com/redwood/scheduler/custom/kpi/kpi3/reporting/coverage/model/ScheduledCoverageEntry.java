package com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model;

import com.redwood.scheduler.api.date.DateTimeZone;

public record ScheduledCoverageEntry (
     CoverageKey key,
     long jobId,
     String jobDefinition,
     String scheduleName,
     DateTimeZone nextRunTime
){
}

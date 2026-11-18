package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model;

import java.util.Set;

public class PowerUsersDashboardDto {
    private PowerUsersSummaryDto summary;

    private Set<JobExecutionDto> coverageRows;

    public PowerUsersSummaryDto getSummary() {
        return summary;
    }

    public void setSummary(
            PowerUsersSummaryDto summary) {
        this.summary = summary;
    }

    public Set<JobExecutionDto> getCoverageRows() {
        return coverageRows;
    }

    public void setCoverageRows(
            Set<JobExecutionDto> coverageRows) {
        this.coverageRows = coverageRows;
    }
}

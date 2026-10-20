package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model;

import java.util.List;

public class CoverageDashboardDto {

    private DashboardSummaryDto summary;

    private List<CoverageViewRowDto> coverageRows;

    public DashboardSummaryDto getSummary() {
        return summary;
    }

    public void setSummary(
            DashboardSummaryDto summary) {
        this.summary = summary;
    }

    public List<CoverageViewRowDto> getCoverageRows() {
        return coverageRows;
    }

    public void setCoverageRows(
            List<CoverageViewRowDto> coverageRows) {
        this.coverageRows = coverageRows;
    }
}
package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model;

import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageReport;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;

import java.util.List;
import java.util.Set;

public class DashboardContext {

    private String period;

    private Set<JobReportingRecord> jobReportingRecords;

    private CoverageReport coverageReport;

    private DashboardSummaryDto summary;

    private List<CoverageViewRowDto> coverageRows;

    public DashboardContext() {
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public CoverageReport getCoverageReport() {
        return coverageReport;
    }

    public void setCoverageReport(
            CoverageReport coverageReport) {

        this.coverageReport = coverageReport;
    }

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

    public Set<JobReportingRecord> getJobReportingRecords() {
        return jobReportingRecords;
    }

    public void setJobReportingRecords(Set<JobReportingRecord> jobReportingRecords) {
        this.jobReportingRecords = jobReportingRecords;
    }
}
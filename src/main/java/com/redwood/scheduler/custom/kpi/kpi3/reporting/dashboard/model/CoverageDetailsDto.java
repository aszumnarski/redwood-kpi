package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model;

import java.util.List;

public class CoverageDetailsDto {

    private CoverageViewRowDto coverage;

    private SummaryMetricsDto summary;

    private List<JobExecutionDto> jobHistory;

    public CoverageViewRowDto getCoverage() {
        return coverage;
    }

    public void setCoverage(CoverageViewRowDto coverage) {
        this.coverage = coverage;
    }

    public SummaryMetricsDto getSummary() {
        return summary;
    }

    public void setSummary(SummaryMetricsDto summary) {
        this.summary = summary;
    }

    public List<JobExecutionDto> getJobHistory() {
        return jobHistory;
    }

    public void setJobHistory(List<JobExecutionDto> jobHistory) {
        this.jobHistory = jobHistory;
    }
}

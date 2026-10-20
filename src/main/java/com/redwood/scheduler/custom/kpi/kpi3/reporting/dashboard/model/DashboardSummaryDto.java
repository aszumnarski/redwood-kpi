package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model;

import java.util.List;

public class DashboardSummaryDto {

    private long configuredCount;

    private long coveredCount;

    private long gapCount;

    private double coveragePercentage;

    private long coeCoveredCount;

    private long businessCoveredCount;

    private long plannedCount;

    private long unexpectedExecutionCount;

    private long totalExecutionCount;

    private List<DashboardBreakdownDto> gapsByCategory;

    private List<DashboardBreakdownDto> gapsByCompany;

    private List<DashboardBreakdownDto> gapsByFrequency;

    public DashboardSummaryDto() {
    }

    public long getConfiguredCount() {
        return configuredCount;
    }

    public void setConfiguredCount(long configuredCount) {
        this.configuredCount = configuredCount;
    }

    public long getCoveredCount() {
        return coveredCount;
    }

    public void setCoveredCount(long coveredCount) {
        this.coveredCount = coveredCount;
    }

    public long getGapCount() {
        return gapCount;
    }

    public void setGapCount(long gapCount) {
        this.gapCount = gapCount;
    }

    public double getCoveragePercentage() {
        return coveragePercentage;
    }

    public void setCoveragePercentage(double coveragePercentage) {
        this.coveragePercentage = coveragePercentage;
    }

    public long getCoeCoveredCount() {
        return coeCoveredCount;
    }

    public void setCoeCoveredCount(long coeCoveredCount) {
        this.coeCoveredCount = coeCoveredCount;
    }

    public long getBusinessCoveredCount() {
        return businessCoveredCount;
    }

    public void setBusinessCoveredCount(long businessCoveredCount) {
        this.businessCoveredCount = businessCoveredCount;
    }

    public long getPlannedCount() {
        return plannedCount;
    }

    public void setPlannedCount(long plannedCount) {
        this.plannedCount = plannedCount;
    }

    public long getUnexpectedExecutionCount() {
        return unexpectedExecutionCount;
    }

    public void setUnexpectedExecutionCount(long unexpectedExecutionCount) {
        this.unexpectedExecutionCount = unexpectedExecutionCount;
    }

    public long getTotalExecutionCount() {
        return totalExecutionCount;
    }

    public void setTotalExecutionCount(long totalExecutionCount) {
        this.totalExecutionCount = totalExecutionCount;
    }

    public List<DashboardBreakdownDto> getGapsByCategory() {
        return gapsByCategory;
    }

    public void setGapsByCategory(
            List<DashboardBreakdownDto> gapsByCategory) {
        this.gapsByCategory = gapsByCategory;
    }

    public List<DashboardBreakdownDto> getGapsByCompany() {
        return gapsByCompany;
    }

    public void setGapsByCompany(
            List<DashboardBreakdownDto> gapsByCompany) {
        this.gapsByCompany = gapsByCompany;
    }

    public List<DashboardBreakdownDto> getGapsByFrequency() {
        return gapsByFrequency;
    }

    public void setGapsByFrequency(
            List<DashboardBreakdownDto> gapsByFrequency) {
        this.gapsByFrequency = gapsByFrequency;
    }

    @Override
    public String toString() {
        return "DashboardSummaryDto{" +
                "configuredCount=" + configuredCount +
                ", coveredCount=" + coveredCount +
                ", gapCount=" + gapCount +
                ", coveragePercentage=" + coveragePercentage +
                ", coeCoveredCount=" + coeCoveredCount +
                ", businessCoveredCount=" + businessCoveredCount +
                ", plannedCount=" + plannedCount +
                ", unexpectedExecutionCount=" + unexpectedExecutionCount +
                ", totalExecutionCount=" + totalExecutionCount +
                '}';
    }
}
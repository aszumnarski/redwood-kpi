package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.service;

import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageReport;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageStatus;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.CoverageDashboardDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.CoverageViewRowDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.DashboardBreakdownDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.DashboardSummaryDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardMapper {


    public CoverageDashboardDto map(CoverageReport report) {

        CoverageDashboardDto dashboard = new CoverageDashboardDto();

        dashboard.setSummary(buildSummary(report));

        dashboard.setCoverageRows(buildCoverageRows(report));

        return dashboard;
    }

    public DashboardSummaryDto buildSummary(CoverageReport report) {

        DashboardSummaryDto dto = new DashboardSummaryDto();

        dto.setConfiguredCount(report.getConfiguredCount());
        dto.setCoveredCount(report.getConfiguredCount() - report.getConfiguredOnlyCount());
        dto.setGapCount(report.getConfiguredOnlyCount());
        dto.setCoveragePercentage(calculateCoveragePercentage(report));
        dto.setCoeCoveredCount(report.getConfiguredScheduledExecutedCount());
        dto.setBusinessCoveredCount(report.getConfiguredExecutedNotScheduledCount());
        dto.setPlannedCount(report.getConfiguredScheduledNotExecutedCount());
        dto.setUnexpectedExecutionCount(report.getUnexpectedExecutionCount());
        dto.setTotalExecutionCount(report.getTotalExecutionCount());
        dto.setGapsByCategory(buildCategoryGaps(report));
        dto.setGapsByCompany(buildCompanyGaps(report));
        dto.setGapsByFrequency(buildFrequencyGaps(report));

        return dto;
    }

    public List<CoverageViewRowDto> buildCoverageRows(CoverageReport report) {

        return report.stream()
                .map(this::toCoverageRow)
                .collect(Collectors.toList());
    }

    public CoverageViewRowDto toCoverageRow(
            CoverageStatus status) {

        CoverageViewRowDto dto = new CoverageViewRowDto();

        dto.setCompanyCode(status.getCompanyCode());
        dto.setAccountItem(status.getAccountItem());
        dto.setCategory(status.getCategory());
        dto.setFrequency(status.getFrequency());
        dto.setReconType(status.getReconType());
        dto.setExecutionCount(status.getExecutionCount());
        dto.setCoverageStatus(determineCoverageStatus(status));

        return dto;
    }

    private double calculateCoveragePercentage(
            CoverageReport report) {

        long configured =
                report.getConfiguredCount();

        if (configured == 0) {
            return 100d;
        }

        long covered =
                configured
                        - report.getConfiguredOnlyCount();

        return covered * 100d / configured;
    }

    private List<DashboardBreakdownDto> buildCategoryGaps(
            CoverageReport report) {

        return report.stream()
                .filter(this::isGap)
                .collect(Collectors.groupingBy(
                        CoverageStatus::getCategory,
                        Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, Long>comparingByValue()
                                .reversed())
                .map(this::toBreakdown)
                .collect(Collectors.toList());
    }

    private List<DashboardBreakdownDto> buildCompanyGaps(
            CoverageReport report) {

        return report.stream()
                .filter(this::isGap)
                .collect(Collectors.groupingBy(
                        CoverageStatus::getCompanyCode,
                        Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, Long>comparingByValue()
                                .reversed())
                .map(this::toBreakdown)
                .collect(Collectors.toList());
    }

    private List<DashboardBreakdownDto> buildFrequencyGaps(
            CoverageReport report) {

        return report.stream()
                .filter(this::isGap)
                .collect(Collectors.groupingBy(
                        CoverageStatus::getFrequency,
                        Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, Long>comparingByValue()
                                .reversed())
                .map(this::toBreakdown)
                .collect(Collectors.toList());
    }

    private DashboardBreakdownDto toBreakdown(
            Map.Entry<String, Long> entry) {

        return new DashboardBreakdownDto(
                entry.getKey(),
                entry.getValue());
    }

    private boolean isGap(
            CoverageStatus status) {

        return status.isConfigured()
                && !status.isExecuted()
                && !status.isScheduled();
    }

    private String determineCoverageStatus(
            CoverageStatus status) {

        if (status.isConfigured()
                && status.isScheduled()
                && status.isExecuted()) {

            return "CoE Covered";
        }

        if (status.isConfigured()
                && status.isScheduled()
                && !status.isExecuted()) {

            return "Planned";
        }

        if (status.isConfigured()
                && !status.isScheduled()
                && status.isExecuted()) {

            return "Business Covered";
        }

        if (status.isConfigured()
                && !status.isScheduled()
                && !status.isExecuted()) {

            return "Gap";
        }

        if (!status.isConfigured()
                && status.isExecuted()) {

            return "Unexpected Execution";
        }

        return "Unknown";
    }
}
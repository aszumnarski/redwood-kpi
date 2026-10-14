package com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.service;

import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.ReconConfiguration;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageKey;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageReport;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageStatus;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;

import java.util.List;
import java.util.Set;

public class CoverageBuilderService {

    public CoverageReport build(Set<ReconConfiguration> configurations, List<JobReportingRecord> records) {

        CoverageReport report = new CoverageReport();
        loadConfigurations(configurations, report);
        loadExecutions(records, report);

        return report;
    }

    private void loadConfigurations(
            Set<ReconConfiguration> configurations,
            CoverageReport report) {

        for (ReconConfiguration config : configurations) {

            CoverageKey key = CoverageKey.from(config);
            CoverageStatus status = report.getOrCreate(key);

            status.setConfigured(true);
            status.setCompanyCode(config.getCompanyCode());
            status.setAccountItem(config.getAccountItem());
            status.setFrequency(config.getFrequency());
            status.setCategory(config.getAccountCategory());
            status.setReconType(config.getReconType());
        }
    }

    private void loadExecutions(
            List<JobReportingRecord> records,
            CoverageReport report) {

        for (JobReportingRecord record : records) {

            CoverageKey key = CoverageKey.from(record);
            CoverageStatus status = report.getOrCreate(key);

            status.incrementExecutionCount();
        }
    }
}
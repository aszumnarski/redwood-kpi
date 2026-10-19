package com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.service;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.MasterTableDefinitionRepository;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.ReconConfiguration;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.ReconConfigurationProvider;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.ReconConfigurationRepository;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageReport;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.ScheduledCoverageEntry;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.repository.ScheduledCoverageRepository;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository.JobReportingRepository;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.text.ParseException;
import java.util.Set;

public class CoverageReportProvider {

    public CoverageReport getCoverageReport(SchedulerSession session, Logger log, String period) throws ParseException {

        MasterTableDefinitionRepository masterTableDefinitionRepository = new MasterTableDefinitionRepository();
        ReconConfigurationRepository reconConfigurationRepository = new ReconConfigurationRepository();
        ReconConfigurationProvider provider = new ReconConfigurationProvider(masterTableDefinitionRepository, reconConfigurationRepository);

        Set<ReconConfiguration> configurations = provider.loadReconConfigurations(session,log);

        Set<JobReportingRecord> records = new JobReportingRepository().findByPeriod(session, period);

        Set<ScheduledCoverageEntry> scheduledEntries = new ScheduledCoverageRepository().findCoveredConfigurations(session, log);

        return new CoverageBuilderService().build(configurations, records, scheduledEntries);
    }
}

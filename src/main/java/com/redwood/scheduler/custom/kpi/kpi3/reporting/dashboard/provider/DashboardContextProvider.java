package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.provider;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.MasterTableDefinitionRepository;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.ReconConfiguration;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.ReconConfigurationProvider;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.ReconConfigurationRepository;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageReport;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.ScheduledCoverageEntry;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.repository.ScheduledCoverageRepository;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.service.CoverageBuilderService;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.CoverageViewRowDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.DashboardContext;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.DashboardSummaryDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.service.CoverageDashboardBuilder;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository.JobReportingRepository;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DashboardContextProvider {

    private static final Map<String, DashboardContext> CACHE = new ConcurrentHashMap<>();

    private final MasterTableDefinitionRepository masterTableDefinitionRepository = new MasterTableDefinitionRepository();
    private final ReconConfigurationRepository reconConfigurationRepository = new ReconConfigurationRepository();
    private final ReconConfigurationProvider reconConfigurationProvider = new ReconConfigurationProvider(masterTableDefinitionRepository, reconConfigurationRepository);
    private final JobReportingRepository jobReportingRepository = new JobReportingRepository();
    private final ScheduledCoverageRepository scheduledCoverageRepository = new ScheduledCoverageRepository();
    private final CoverageBuilderService coverageBuilderService = new CoverageBuilderService();
    private final CoverageDashboardBuilder coverageDashboardBuilder = new CoverageDashboardBuilder();

    public DashboardContext getContext(SchedulerSession session, String period, Logger log) throws ParseException {

        DashboardContext cached = CACHE.get(period);

        if (cached != null) {

            log.info("Dashboard context cache hit for period " + period);
            return cached;
        }

        log.info("Dashboard context cache miss for period " + period);

        DashboardContext context = buildContext(session, period, log);

        CACHE.put(period, context);

        return context;
    }

    private DashboardContext buildContext(SchedulerSession session, String period, Logger log) throws ParseException {

        Set<ReconConfiguration> configurations = reconConfigurationProvider.loadReconConfigurations(session, log);
        Set<JobReportingRecord> records = jobReportingRepository.findByPeriod(session, period);
        Set<ScheduledCoverageEntry> scheduledEntries = scheduledCoverageRepository.findCoveredConfigurations(session, log);
        CoverageReport coverageReport = coverageBuilderService.build(configurations, records, scheduledEntries);
        DashboardSummaryDto summary = coverageDashboardBuilder.buildSummary(coverageReport);
        List<CoverageViewRowDto> rows = coverageDashboardBuilder.buildCoverageRows(coverageReport);

        DashboardContext context = new DashboardContext();

        context.setPeriod(period);
        context.setJobReportingRecords(records);
        context.setCoverageReport(coverageReport);
        context.setSummary(summary);
        context.setCoverageRows(rows);

        return context;
    }

    public static void clearCache() {
        CACHE.clear();
    }

    public static int size() {
        return CACHE.size();
    }


}

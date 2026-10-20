package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.provider;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageReport;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.service.CoverageReportProvider;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.CoverageDashboardDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.service.DashboardMapper;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.text.ParseException;

public class CoverageDashboardProvider {

    private final CoverageReportProvider coverageReportProvider = new CoverageReportProvider();
    private final DashboardMapper dashboardMapper = new DashboardMapper();

    public CoverageDashboardDto getDashboard(SchedulerSession session, String period, Logger log) throws ParseException {

        CoverageReport report = coverageReportProvider.getCoverageReport(session, period, log);

        return dashboardMapper.map(report);
    }
}

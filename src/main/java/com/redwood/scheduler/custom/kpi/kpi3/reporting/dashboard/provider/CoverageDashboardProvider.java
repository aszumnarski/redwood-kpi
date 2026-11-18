package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.provider;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.CoverageDashboardDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.DashboardContext;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.text.ParseException;

public class CoverageDashboardProvider {

    private final DashboardContextProvider contextProvider = new DashboardContextProvider();

    public CoverageDashboardDto getDashboard(SchedulerSession session, String period, Logger log) throws ParseException {

        DashboardContext context = contextProvider.getContext(session, period, log);

        CoverageDashboardDto dto = new CoverageDashboardDto();

        dto.setSummary(context.getSummary());

        dto.setCoverageRows(context.getCoverageRows());

        return dto;
    }
}

package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.provider;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.DashboardContext;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.PowerUsersDashboardDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.service.PowerUsersDashboardMapper;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.text.ParseException;


public class PowerUsersDashboardProvider {

    private final DashboardContextProvider contextProvider = new DashboardContextProvider();
    private final PowerUsersDashboardMapper mapper = new PowerUsersDashboardMapper();

    public PowerUsersDashboardDto getDashboard(SchedulerSession session, String period, Logger log) throws ParseException {

        DashboardContext context = contextProvider.getContext(session, period, log);

        return mapper.map(context.getJobReportingRecords(), context.getPeriod());
    }
}

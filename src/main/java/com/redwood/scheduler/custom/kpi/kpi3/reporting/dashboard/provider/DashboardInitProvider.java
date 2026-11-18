package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.provider;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.DashboardInitDto;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

public class DashboardInitProvider {

    public DashboardInitDto getInit(SchedulerSession session, Logger log) {

        DashboardInitDto dto = new DashboardInitDto();
        dto.setPeriods(loadAvailablePeriods(session));
        dto.setDefaultPeriod(dto.getPeriods().isEmpty() ? null : dto.getPeriods().get(0));
        dto.setCompanyCodes(loadAvailableCompanyCodes(session));

        return dto;
    }
}
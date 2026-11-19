package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.config;

import com.redwood.scheduler.api.model.SchedulerSession;

import java.util.Map;

public class ClearingDashboardConfigService {

    private final ClearingDashboardConfigRepository repository = new ClearingDashboardConfigRepository();

    public ClearingDashboardConfig load(SchedulerSession session) {
        Map<String, String> values = repository.loadAll(session);

        return new ClearingDashboardConfig(values.get("html"),values.get("js"));
    }

}
